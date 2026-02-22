package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText firstnameEditText;   // define confirm edittext variable
    private EditText lastnameEditText;
    private EditText emailEditText;

    private Button createAccountButton;         // define login button variable
    private Button backToLoginButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.signup_username);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password);  // link to password edtext in the Signup activity XML
        firstnameEditText = findViewById(R.id.signup_firstname);    // link to confirm edtext in the Signup activity XML
        lastnameEditText = findViewById(R.id.signup_lastname);
        emailEditText = findViewById(R.id.signup_email);
        createAccountButton = findViewById(R.id.create_account_btn);    // link to login button in the Signup activity XML
        backToLoginButton = findViewById(R.id.back_to_login_btn);  // link to signup button in the Signup activity XML

        /* click listener on login button pressed */
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);  // go to LoginActivity
            }
        });

        /* click listener on signup button pressed */
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* Checking if the username and password are valid */
//                if (username.isEmpty()){
//                    Toast.makeText(getApplicationContext(), "Username is empty", Toast.LENGTH_LONG).show();
//                } else if (password.isEmpty() || confirm.isEmpty()){
//                    Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_LONG).show();
//                }
//                else if (!password.equals(confirm)){
//                    Toast.makeText(getApplicationContext(), "Password don't match", Toast.LENGTH_LONG).show();
//                }
//                else if (password.length() < 8){
//                    Toast.makeText(getApplicationContext(), "Password must be 8+ characters", Toast.LENGTH_LONG).show();
//                }
//                else if (!password.matches(".*[^a-zA-Z0-9].*")){
//                    Toast.makeText(getApplicationContext(), "Password must contain a symbol", Toast.LENGTH_LONG).show();
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "Signing up", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }
}