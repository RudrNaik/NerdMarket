package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
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

import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class CameraSearchActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageButton captureButton;
    private ImageCapture imageCapture;
    private static final int CAMERA_PERMISSION_CODE = 309;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerasearch);

        previewView = findViewById(R.id.cameraSearch_previewView);
        captureButton = findViewById(R.id.cameraSearch_camera_capture_btn);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
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
                    Bitmap nameRegion = cropCardForNameOnly(bitmap);
                    Bitmap processedForTessy = processImageForTessy(nameRegion);
                    String cardName = OCRCroppedName(processedForTessy);
                    Log.d("OCR", "Tessy got: " + cardName);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Card Name: " + cardName, Toast.LENGTH_LONG).show());
                }).start();
            }

            @Override
            public void onError(ImageCaptureException e){
                e.printStackTrace();
            }
        });
    }

    private Bitmap cropCardForNameOnly(Bitmap fullImage){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap portraitImage = Bitmap.createBitmap(fullImage, 0, 0, fullImage.getWidth(), fullImage.getHeight(), matrix, true);

        int screenWidth = portraitImage.getWidth();
        int screenHeight = portraitImage.getHeight();

        Log.d("CropDebug", "Image: " + screenWidth + "x" + screenHeight);

        double cardWidthFrame = .8;
        double aspectRatio = 2.5/3.5;

        int cardWidth = (int) (screenWidth * cardWidthFrame);
        int cardHeight = (int) (cardWidth / aspectRatio);

        int cardLeft = (screenWidth - cardWidth) / 2;
        int cardTop = (screenHeight - cardHeight) / 2;
        int nameHeight = (int) (cardHeight * .06);

        Log.d("CropDebug", "cardLeft=" + cardLeft + " cardTop=" + cardTop + " cardWidth=" + cardWidth + " nameHeight=" + nameHeight);

        return Bitmap.createBitmap(fullImage, cardLeft, cardTop, cardWidth, nameHeight);
    }

    private String OCRCroppedName(Bitmap croppedBitmap){
        TessBaseAPI tess = new TessBaseAPI();
        File dir = new File(getFilesDir(), "tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
            copyTessDataFiles(dir);
        }
        tess.init(getFilesDir().getAbsolutePath(), "eng");
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
}
