package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;

import android.os.Environment;
import android.util.Base64;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import android.os.Bundle;



public class CameraSearchActivity extends AppCompatActivity {
    private PreviewView previewView;
    private String  username;
    private int  id;
    private boolean isAdmin;
    private ImageButton captureButton;
    private ImageButton backtoSearchButton;
    private ImageCapture imageCapture;
    private static final int CAMERA_PERMISSION_CODE = 309;
    private String WEBSOCKET_URL = "ws://coms-3090-022.class.las.iastate.edu:8080/ws/scanning";
    private StompClient stompClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerasearch);

        previewView = findViewById(R.id.cameraSearch_previewView);
        captureButton = findViewById(R.id.cameraSearch_camera_capture_btn);
        backtoSearchButton = findViewById(R.id.cameraSearch_back_btn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            id       = extras.getInt("id", -1);
            isAdmin  = extras.getBoolean("isAdmin", false);
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        backtoSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraSearchActivity.this, CardSearchActivity.class);
            startActivity(intent);
        });

        WebSocketInit();
    }

    private void WebSocketInit() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WEBSOCKET_URL);

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("STOMP", "Stomp connection opened");
                    break;
                case ERROR:
                    Log.e("STOMP", "Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("STOMP", "Stomp connection closed");
                    break;
            }
        });

        stompClient.topic("/topic/scan-result").subscribe(message -> {
            try{
                JSONObject jsonObject = new JSONObject(message.getPayload());

                boolean success = jsonObject.getBoolean("success");
                String msg = jsonObject.getString("message");

                if (success == true){
                    JSONArray cards = jsonObject.getJSONArray("cards");
                    JSONObject topCard = cards.getJSONObject(0);
                    int confidence = topCard.getInt("confidence");
                    JSONObject card = topCard.getJSONObject("card");
                    int bundleCardID = card.getInt("id");
                    String returnedCardName = card.getString("cardName");

                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Match: " + returnedCardName + " Confidence: " + confidence, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(CameraSearchActivity.this, CardSearchActivity.class);
                        intent.putExtra("bundleCardID", bundleCardID);
                        intent.putExtra("id", id);
                        intent.putExtra("username", username);
                        intent.putExtra("isAdmin", isAdmin);
                        startActivity(intent);

                        Log.d("STOMP", "Match: " + returnedCardName + " Confidence: " + confidence);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No match" + msg, Toast.LENGTH_LONG).show());
                    Log.d("STOMP", "No match " + msg);
                }
            } catch (JSONException e){
                Log.e("STOMP", String.valueOf(e));
            }
        });

        stompClient.connect();
    }

    private void sendScanAndName(Bitmap bitmap, String cardName){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
        String payload = "{\"imageBase64\":\"" + base64Image + "\"," + "\"cardNameOcr\":\"" + cardName + "\"}";
        Log.d("STOMP", "Payload size: " + payload.getBytes().length + " bytes");
        Log.d("STOMP", "Text sent: " + cardName);
        stompClient.send("/app/scan", payload).subscribe(
                () -> Log.d("STOMP", "Scan sent"),
                error -> Log.e("STOMP", "Send failed", error)
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null){
            stompClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(getApplicationContext(), "Camera permission was denied", Toast.LENGTH_LONG).show();
        }
    }

    void startCamera(){

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setTargetRotation(Surface.ROTATION_0).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);

            } catch (Exception e){
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(this));
    }

    void takePhoto(){
        if (imageCapture == null){
            return;
        }
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback(){
            @Override
            public void onCaptureSuccess(ImageProxy imageProxy){
                Bitmap bitmap = imageProxy.toBitmap();
                imageProxy.close();
                Toast.makeText(getApplicationContext(), "Photo taken", Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap portraitImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    bitmap.recycle();

                    Bitmap fullCardRegion = cropImageForCard(portraitImage);
                    Bitmap nameRegion = cropCardForNameOnly(portraitImage);
                    portraitImage.recycle();

                    Bitmap processedForTessy = processImageForTessy(nameRegion);
                    nameRegion.recycle();

                    String cardName = OCRCroppedName(processedForTessy);
                    Log.d("OCR", "Tessy got: " + cardName);
                    debugSaveBitmap(processedForTessy, "tessy_input");
                    processedForTessy.recycle();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Card Name: " + cardName, Toast.LENGTH_LONG).show());
                    sendScanAndName(fullCardRegion, cardName);
                    fullCardRegion.recycle();
                }).start();
            }

            @Override
            public void onError(ImageCaptureException e){
                e.printStackTrace();
            }
        });
    }

    private Bitmap cropCardForNameOnly(Bitmap portraitImage){

        int screenWidth = portraitImage.getWidth();
        int screenHeight = portraitImage.getHeight();

        Log.d("CropDebug", "Image: " + screenWidth + "x" + screenHeight);

        int cardLeft   = (int) (screenWidth  * 0.27);
        int cardTop    = (int) (screenHeight * 0.29);
        int cardWidth  = (int) (screenWidth  * 0.45);
        int cardHeight = (int) (screenHeight * 0.48);

        Log.d("CropDebug", "cardLeft=" + cardLeft + " cardTop=" + cardTop + " cardWidth=" + cardWidth + " nameHeight=" + cardHeight);

        int nameLeft   = cardLeft + (int) (cardWidth * 0.15);
        int nameTop    = cardTop;
        int nameWidth  = (int) (cardWidth * 0.40);
        int nameHeight = (int) (cardHeight * 0.06);

        return Bitmap.createBitmap(portraitImage, nameLeft, nameTop, nameWidth, nameHeight);
    //    return Bitmap.createBitmap(portraitImage, cardLeft, cardTop, cardWidth, cardHeight);

    }

    private Bitmap cropImageForCard(Bitmap portraitImage){

        int screenWidth = portraitImage.getWidth();
        int screenHeight = portraitImage.getHeight();

        Log.d("CropDebug", "Image: " + screenWidth + "x" + screenHeight);

        int cardLeft   = (int) (screenWidth  * 0.27);
        int cardTop    = (int) (screenHeight * 0.29);
        int cardWidth  = (int) (screenWidth  * 0.45);
        int cardHeight = (int) (screenHeight * 0.48);

        Log.d("CropDebug", "cardLeft=" + cardLeft + " cardTop=" + cardTop + " cardWidth=" + cardWidth + " nameHeight=" + cardHeight);

        return Bitmap.createBitmap(portraitImage, cardLeft, cardTop, cardWidth, cardHeight);
    }

    private String OCRCroppedName(Bitmap croppedBitmap){
        TessBaseAPI tess = new TessBaseAPI();
        File dir = new File(getFilesDir(), "tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
            copyTessDataFiles(dir);
        }
        dir.mkdirs();
        copyTessDataFiles(dir);
        tess.init(getFilesDir().getAbsolutePath(), "eng");
        tess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz -");

        tess.setImage(croppedBitmap);
        String result = tess.getUTF8Text().trim();
        tess.recycle();

        return result;
    }


    //Helper method based on: https://stackoverflow.com/questions/3373860/convert-a-bitmap-to-grayscale-in-android
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private Bitmap processImageForTessy(Bitmap bitmap){
        Bitmap grayscale = toGrayscale(bitmap);
        return Bitmap.createScaledBitmap(grayscale, bitmap.getWidth() * 2, bitmap.getHeight() * 2, true);
    }

    //Used from https://transloadit.com/devtips/ocr-android-sdk/
    //Helper method from article called "Implementing real-time text recognition
    //in Android apps with OpenCV and Tesseract"
    private void copyTessDataFiles(File dir) {
        try {
            AssetManager assetManager = getAssets();
            String[] fileList = assetManager.list("tessdata");
            if (fileList != null) {
                for (String fileName : fileList) {
                    File file = new File(dir, fileName);
                    if (!file.exists()) {
                        InputStream in = assetManager.open("tessdata/" + fileName);
                        OutputStream out = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            }
        } catch (IOException e) {
            Log.e("OCR", "Error copying tess data files", e);
        }
    }

    private void debugSaveBitmap(Bitmap bitmap, String filename) {
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.d("DEBUG", "Saved bitmap to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("DEBUG", "Failed to save bitmap", e);
        }
    }

}
