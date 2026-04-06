package NerdMarket.scanning;

import NerdMarket.market.Market;
import NerdMarket.market.MarketRepository;
import org.opencv.core.*;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScanningService {

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    @Autowired
    private MarketRepository marketRepository;

    private static final int ORB_FEATURES  = 1000;
    private static final float RATIO_THRESH = 0.75f;
    private static final int MAX_CACHE_SIZE = 500;

    // Bounded LRU cache: releases native Mat memory when entries are evicted
    private final Map<Long, Mat> descriptorCache = Collections.synchronizedMap(
        new LinkedHashMap<Long, Mat>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Mat> eldest) {
                if (size() > MAX_CACHE_SIZE) {
                    eldest.getValue().release();
                    return true;
                }
                return false;
            }
        }
    );

    @PostConstruct
    public void warmCache() {
        new Thread(() -> {
            List<Market> cards = marketRepository.findAll();
            int warmed = 0;
            for (Market card : cards) {
                if (warmed >= MAX_CACHE_SIZE) break;
                if (card.getImageUrl() != null) {
                    Mat descriptors = computeDescriptors(card.getImageUrl());
                    if (descriptors != null && !descriptors.empty()) {
                        descriptorCache.put(card.getId(), descriptors);
                        warmed++;
                    }
                }
            }
            System.out.println("[ScanningService] Descriptor cache warmed: " + descriptorCache.size() + " cards");
        }, "descriptor-cache-warmer").start();
    }

    public void cacheCard(Market card) {
        if (card.getImageUrl() != null) {
            Mat descriptors = computeDescriptors(card.getImageUrl());
            if (descriptors != null && !descriptors.empty()) {
                descriptorCache.put(card.getId(), descriptors);
            }
        }
    }

    public ScanningResponse processCardScan(ScanningRequest request) {
        if (request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            return new ScanningResponse(false, "No image provided", null);
        }

        Mat queryImage = decodeBase64ToMat(request.getImageBase64());
        if (queryImage == null || queryImage.empty()) {
            return new ScanningResponse(false, "Could not decode image", null);
        }

        try {
            String ocrName = request.getCardNameOcr();
            List<Market> candidates = (ocrName != null && !ocrName.isBlank())
                    ? marketRepository.findByCardNameContainingIgnoreCase(ocrName)
                    : marketRepository.findAll();

            if (candidates.isEmpty()) {
                return new ScanningResponse(false, "No cards found for: " + ocrName, null);
            }

            List<ScanningMatch> matches = findBestMatches(queryImage, candidates);
            if (matches.isEmpty()) {
                return new ScanningResponse(false, "No matching card found", null);
            }

            String message = matches.size() > 1 ? "Multiple close matches found" : "Match found";
            return new ScanningResponse(true, message, matches);
        } finally {
            queryImage.release();
        }
    }

    private List<ScanningMatch> findBestMatches(Mat queryImage, List<Market> cards) {
        ORB orb = ORB.create(ORB_FEATURES);
        BFMatcher matcher = BFMatcher.create(Core.NORM_HAMMING, false);

        MatOfKeyPoint queryKeypoints = new MatOfKeyPoint();
        Mat queryDescriptors = new Mat();
        Mat mask = new Mat();
        orb.detectAndCompute(queryImage, mask, queryKeypoints, queryDescriptors);
        mask.release();

        if (queryDescriptors.empty()) {
            queryKeypoints.release();
            queryDescriptors.release();
            return List.of();
        }

        int queryKeypointCount = queryKeypoints.toList().size();
        queryKeypoints.release();

        Map<Market, Double> scores = new LinkedHashMap<>();

        for (Market card : cards) {
            Mat cardDescriptors = descriptorCache.computeIfAbsent(card.getId(), id -> {
                if (card.getImageUrl() == null) return new Mat();
                Mat d = computeDescriptors(card.getImageUrl());
                return d != null ? d : new Mat();
            });

            if (cardDescriptors.empty()) continue;

            List<MatOfDMatch> knnMatches = new ArrayList<>();
            matcher.knnMatch(queryDescriptors, cardDescriptors, knnMatches, 2);

            long goodMatches = knnMatches.stream()
                    .filter(m -> {
                        DMatch[] arr = m.toArray();
                        return arr.length >= 2 && arr[0].distance < RATIO_THRESH * arr[1].distance;
                    })
                    .count();

            knnMatches.forEach(Mat::release);

            if (queryKeypointCount > 0) {
                double score = (double) goodMatches / queryKeypointCount;
                scores.put(card, score);
            }
        }

        queryDescriptors.release();

        if (scores.isEmpty()) return List.of();

        double topScore = Collections.max(scores.values());

        return scores.entrySet().stream()
                .filter(e -> e.getValue() >= topScore * 0.1)
                .sorted(Map.Entry.<Market, Double>comparingByValue().reversed())
                .limit(5)
                .map(e -> new ScanningMatch(e.getKey(), (int) Math.round((e.getValue() / topScore) * 100)))
                .collect(Collectors.toList());
    }

    private Mat computeDescriptors(String imageUrl) {
        Mat image = null;
        try {
            ORB orb = ORB.create(ORB_FEATURES);
            image = downloadImageToMat(imageUrl);
            if (image == null || image.empty()) return null;
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            Mat mask = new Mat();
            orb.detectAndCompute(image, mask, keypoints, descriptors);
            mask.release();
            keypoints.release();
            return descriptors;
        } catch (Exception e) {
            return null;
        } finally {
            if (image != null) image.release();
        }
    }

    private Mat decodeBase64ToMat(String imageBase64) {
        try {
            String base64Data = imageBase64.contains(",") ? imageBase64.split(",")[1] : imageBase64;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            MatOfByte mob = new MatOfByte(imageBytes);
            Mat result = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_GRAYSCALE);
            mob.release();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> debugScan(ScanningRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            result.put("error", "No image provided");
            return result;
        }

        Mat image = decodeBase64ToMat(request.getImageBase64());
        if (image == null || image.empty()) {
            result.put("error", "Could not decode image");
            return result;
        }

        try {
            // Image info
            result.put("imageWidth",    image.cols());
            result.put("imageHeight",   image.rows());
            result.put("imageChannels", image.channels());

            // ORB detection
            ORB orb = ORB.create(500);
            MatOfKeyPoint matKeypoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            Mat mask = new Mat();
            orb.detectAndCompute(image, mask, matKeypoints, descriptors);
            mask.release();

            int descRows = descriptors.rows();
            int descCols = descriptors.cols();
            descriptors.release();

            List<KeyPoint> kps = matKeypoints.toList();
            matKeypoints.release();

            result.put("orbMaxFeatures",  500);
            result.put("keypointCount",   kps.size());
            result.put("descriptorRows",  descRows);
            result.put("descriptorCols",  descCols); // 32 cols = 256-bit ORB descriptor

            // Response stats (how distinctive each keypoint is — higher = better)
            if (!kps.isEmpty()) {
                DoubleSummaryStatistics responseStats = kps.stream()
                        .mapToDouble(kp -> kp.response)
                        .summaryStatistics();
                result.put("responseMin",  responseStats.getMin());
                result.put("responseMax",  responseStats.getMax());
                result.put("responseAvg",  responseStats.getAverage());

                // Scale distribution — how many keypoints per octave level
                Map<Integer, Long> octaveDist = kps.stream()
                        .collect(Collectors.groupingBy(kp -> kp.octave, TreeMap::new, Collectors.counting()));
                result.put("octaveDistribution", octaveDist);

                // Spatial distribution — divide image into 3x3 grid, count keypoints per cell
                int cellW = image.cols() / 3;
                int cellH = image.rows() / 3;
                int[][] grid = new int[3][3];
                for (KeyPoint kp : kps) {
                    int col = Math.min((int)(kp.pt.x / cellW), 2);
                    int row = Math.min((int)(kp.pt.y / cellH), 2);
                    grid[row][col]++;
                }
                List<Map<String, Object>> spatialGrid = new ArrayList<>();
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        Map<String, Object> cell = new LinkedHashMap<>();
                        cell.put("cell",  "row" + r + "_col" + c);
                        cell.put("count", grid[r][c]);
                        spatialGrid.add(cell);
                    }
                }
                result.put("spatialDistribution", spatialGrid);

                // Top 10 keypoints by response score
                List<Map<String, Object>> topKps = kps.stream()
                        .sorted(Comparator.comparingDouble((KeyPoint kp) -> kp.response).reversed())
                        .limit(10)
                        .map(kp -> {
                            Map<String, Object> k = new LinkedHashMap<>();
                            k.put("x",        kp.pt.x);
                            k.put("y",        kp.pt.y);
                            k.put("response", kp.response);
                            k.put("size",     kp.size);
                            k.put("angle",    kp.angle);
                            k.put("octave",   kp.octave);
                            return k;
                        })
                        .collect(Collectors.toList());
                result.put("topKeypoints", topKps);
            }
        } finally {
            image.release();
        }

        return result;
    }

    private Mat downloadImageToMat(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            byte[] imageBytes = in.readAllBytes();
            MatOfByte mob = new MatOfByte(imageBytes);
            Mat result = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_GRAYSCALE);
            mob.release();
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
