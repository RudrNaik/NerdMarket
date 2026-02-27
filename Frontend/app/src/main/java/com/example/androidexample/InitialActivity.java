package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;

public class InitialActivity extends AppCompatActivity{

    Animation topAnim, bottomAnim;
    ImageView image;
    private static int FADE_TIME = 5000;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image = findViewById(R.id.logo_image_welcome);

        image.setAnimation(topAnim);

        new Handler(getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(InitialActivity.this, LoginActivity.class);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation
                            (InitialActivity.this, image, "logo_image_welcome");
            ActivityCompat.startActivity(InitialActivity.this, intent, options.toBundle());
            finish();
        }, FADE_TIME);
    }
}
