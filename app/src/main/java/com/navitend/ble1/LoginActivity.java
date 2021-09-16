package com.navitend.ble1;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Animation rotate_animation;
    ImageView logo_iv;
    private Button login_btn;
    private TextView register_tv;
    private EditText email_et;
    private EditText password_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(this);
        email_et = findViewById(R.id.email_et);
        password_et = findViewById(R.id.password_et);
        logo_iv = findViewById(R.id.logo_iv_login);
        logo_iv.setOnClickListener(this);
        rotateAnimation();
        register_tv = findViewById(R.id.register_tv);
        register_tv.setOnClickListener(this);

    }
    private void switchToMainActivity(String email, String password) {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        switchActivityIntent.putExtra("email",email );
        switchActivityIntent.putExtra("password",password );
        startActivity(switchActivityIntent);

    }
    private void switchToRegisterActivity() {
        Intent switchActivityIntent = new Intent(this, Register.class);
        startActivity(switchActivityIntent);

    }

    private void rotateAnimation(){

        rotate_animation = AnimationUtils.loadAnimation(this,R.anim.rotate);
        logo_iv.startAnimation(rotate_animation);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.login_btn:
                //need to add auth from server
                switchToMainActivity(email_et.getText().toString(), password_et.getText().toString());
                break;
            case R.id.register_tv:
                switchToRegisterActivity();
                break;
            case R.id.logo_iv_login:
                rotateAnimation();
                break;

        }


    }
}