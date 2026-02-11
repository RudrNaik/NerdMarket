package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView numberTxt; // define number textview variable
    private Button increaseBtn; // define increase button variable

    private TextView targetTxt;
    private Button incby5btn; // define decrease button variable
    private Button decreaseBtn; // define decrease button variable
    private Button decby5Btn; // define decrease button variable
    private Button backBtn;     // define back button variable
    private Button zeroOutBtn;     // define back button variable

    private int counter = 0;    // counter variable

    public int randomNum = 15;

    protected void randomSet(int x){
        randomNum = x;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        /* initialize UI elements by using findviewbyid to search the xml file for the buttons.*/
        numberTxt = findViewById(R.id.number);
        targetTxt = findViewById(R.id.target);
        increaseBtn = findViewById(R.id.counter_increase_btn);
        decreaseBtn = findViewById(R.id.counter_decrease_btn);
        incby5btn = findViewById(R.id.counter_incby5_btn);
        decby5Btn = findViewById(R.id.counter_decby5_btn);
        backBtn = findViewById(R.id.counter_back_btn);
        zeroOutBtn = findViewById(R.id.counter_zero_btn);


        targetTxt.setVisibility(TextView.INVISIBLE);


        /* when increase btn is pressed, counter++, reset number textview */
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(counter+=3));
            }
        });

        incby5btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(counter+=5));
            }
        });

        /* when decrease btn is pressed, counter--, reset number textview */
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(counter+=3));
            }
        });

        decby5Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(counter-=5));
            }
        });

        zeroOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberTxt.setText(String.valueOf(counter = 0));
            }
        });


        /* when back btn is pressed, switch back to MainActivity */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CounterActivity.this, MainActivity.class);
                intent.putExtra("NUM", String.valueOf(counter));  // key-value to pass to the MainActivity
                startActivity(intent);
            }
        });

    }
}