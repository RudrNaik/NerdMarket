package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;


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
                imageCapture = new ImageCapture.Builder().build();
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
            }

            @Override
            public void onError(ImageCaptureException e){
                e.printStackTrace();
            }
        });
    }
}
