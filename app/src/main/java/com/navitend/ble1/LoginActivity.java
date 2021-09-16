package com.navitend.ble1;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String tag = "We said that: ";
    Animation rotate_animation;
    ImageView logo_iv;
    private Button login_btn;
    private TextView register_tv;
    private EditText email_et;
    private EditText password_et;
    private ProgressBar progress_bar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(this);
        email_et = findViewById(R.id.email_et);
        password_et = findViewById(R.id.password_et);
        progress_bar = findViewById(R.id.progress_bar);
        logo_iv = findViewById(R.id.logo_iv_login);
        logo_iv.setOnClickListener(this);
        rotateAnimation();
        register_tv = findViewById(R.id.register_tv);
        register_tv.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

    }

    private void switchToMainActivity(String email, String password) {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        switchActivityIntent.putExtra("email", email);
        switchActivityIntent.putExtra("password", password);
        startActivity(switchActivityIntent);

    }

    private void switchToRegisterActivity() {
        Intent switchActivityIntent = new Intent(this, Register.class);
        startActivity(switchActivityIntent);

    }

    private void rotateAnimation() {

        rotate_animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        logo_iv.startAnimation(rotate_animation);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.login_btn:
                userLogin();
                break;
            case R.id.register_tv:
                switchToRegisterActivity();
                break;
            case R.id.logo_iv_login:
                rotateAnimation();
                break;

        }


    }

    private void userLogin() {

        final String email = email_et.getText().toString().trim();
        final String password = password_et.getText().toString().trim();
        if (email.isEmpty()) {
            email_et.setError("Email is required");
            email_et.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            password_et.setError("Password is required");
            password_et.requestFocus();
            return;
        }
        if (password.length() < 6) {
            password_et.setError("Password need to be at least 6 characters long");
            password_et.requestFocus();
            return;
        }
        progress_bar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    switchToMainActivity();
                } else {
                    Toast t  = Toast.makeText(LoginActivity.this, "Failed to login: " + task.getException().getMessage(),  Toast.LENGTH_LONG);
                }
            }
        });

    }
    private void switchToMainActivity() {
        //need to do saving
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
    }
}