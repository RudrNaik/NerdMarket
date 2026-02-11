package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.signup_username_edt);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password_edt);  // link to password edtext in the Signup activity XML
        confirmEditText = findViewById(R.id.signup_confirm_edt);    // link to confirm edtext in the Signup activity XML
        loginButton = findViewById(R.id.signup_login_btn);    // link to login button in the Signup activity XML
        signupButton = findViewById(R.id.signup_signup_btn);  // link to signup button in the Signup activity XML

        TextWatcher loginChecker = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();
                signupButton.setEnabled(!username.isEmpty() && !password.isEmpty() && !confirm.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        signupButton.setEnabled(false);
        usernameEditText.addTextChangedListener(loginChecker);
        passwordEditText.addTextChangedListener(loginChecker);
        confirmEditText.addTextChangedListener(loginChecker);


        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);  // go to LoginActivity
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();

                if (password.equals(confirm) && checkIfvalid(password)){
                    Toast.makeText(getApplicationContext(), "Signed up", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);  // go to LoginActivity
                }
                else if(!password.equals(confirm)) {
                    Toast.makeText(getApplicationContext(), "Password don't match", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Password does not fit criteria", Toast.LENGTH_LONG).show();
                }
            }

            public boolean checkIfvalid(String password){
                String specialChars = "@_!#$%^&*()<>?/|}{~:';";
                int specialCharacters = 0;
                int numbers = 0;
                for (int i = 0; i < password.length(); i++){
                    if (specialChars.contains(Character.toString(password.charAt(i)))){
                        specialCharacters++;
                    }
                    if (Character.isDigit(password.charAt(i))){
                        numbers++;
                    }
                }

                if((numbers > 2) && (specialCharacters >= 1)){
                    return true;
                }

                return false;
            }
        });
    }
}