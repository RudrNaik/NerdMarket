package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ImageController {

    @Value("${cards.upload.dir:${user.home}/card-scans}")
    private String uploadDir;

    @Autowired
    private ImageRepository imageRepository;

    // List all card scans (metadata only)
    @GetMapping("/cards")
    public List<Image> getAllCardScans() {
        return imageRepository.findAll();
    }

    // Retrieve the scanned card image by ID
    @GetMapping(value = "/cards/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    byte[] getCardImageById(@PathVariable int id) throws IOException {
        Image cardScan = imageRepository.findById(id);
        File imageFile = new File(cardScan.getFilePath());
        return Files.readAllBytes(imageFile.toPath());
    }

    // Upload a new card scan image
    @PostMapping("/cards/scan")
    public String handleCardScanUpload(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "cardName", defaultValue = "Unknown Card") String cardName) {

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destinationFile = new File(dir, imageFile.getOriginalFilename());
            imageFile.transferTo(destinationFile);

            Image cardScan = new Image();
            cardScan.setFilePath(destinationFile.getAbsolutePath());
            cardScan.setCardName(cardName);
            cardScan.setScannedAt(LocalDateTime.now());
            imageRepository.save(cardScan);

            return "Card scan uploaded successfully: " + destinationFile.getAbsolutePath();
        } catch (IOException e) {
            return "Failed to upload card scan: " + e.getMessage();
        }
    }
}