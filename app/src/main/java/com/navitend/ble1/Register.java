package com.navitend.ble1;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class Register extends AppCompatActivity implements View.OnClickListener {
    private final String tag = "ble1";
    private FirebaseAuth mAuth;
    private Button register_btn;
    private EditText name_et;
    private EditText email_et;
    private EditText password_et;
    private ProgressBar progress_bar;
    private TextView home_tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        register_btn = findViewById(R.id.register_btn);
        register_btn.setOnClickListener(this);

        name_et = findViewById(R.id.name_reg_et);
        email_et = findViewById(R.id.email_reg_et);
        password_et = findViewById(R.id.password_reg_et);
        progress_bar = findViewById(R.id.progress_bar_reg);
        home_tv = findViewById(R.id.home_reg_tv);
        home_tv.setOnClickListener(this);


    }

    private void registerUser() {
        //need to do saving
        final String name = name_et.getText().toString().trim();
        final String email = email_et.getText().toString().trim();
        String password = password_et.getText().toString().trim();
        if (name.isEmpty()) {
            name_et.setError("Name is required");
            name_et.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            email_et.setError("Email is required");
            email_et.requestFocus();
            return;
        }
//        if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//            email_et.setError("Please provide valid Email address");
//            email_et.requestFocus();
//            return;
//        }
        if (password.isEmpty()) {
            password_et.setError("Password is required");
            password_et.requestFocus();
            return;
        }
        if(password.length()< 6){
            password_et.setError("Password need to be at least 6 characters long");
            password_et.requestFocus();
            return;
        }
        Log.i(tag, "manage to collect name: " + name +" Email: "+email+" password: "+password);
        progress_bar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final User user = new User (name, email);
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.i("reg", "manage to register" + user.toString());
                                Toast.makeText(Register.this,"user has been register successfully", Toast.LENGTH_LONG );
                            progress_bar.setVisibility(View.GONE);
                            }else{
                                Log.i("reg", "did not manage to register" + user.toString());
                                Toast.makeText(Register.this,"Failed to register user, try again", Toast.LENGTH_LONG );
                                progress_bar.setVisibility(View.GONE);
                            }
                        }
                    });

                }else{
                    Toast.makeText(Register.this,"Failed to register user, try again", Toast.LENGTH_LONG );
                    progress_bar.setVisibility(View.GONE);
                }
            }
        });
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);


    }

    private void switchToLoginActivity() {
        //need to do saving
        Intent switchActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(switchActivityIntent);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.register_btn:
                registerUser();
                break;
            case R.id.home_reg_tv:
                switchToLoginActivity();
                break;
        }

    }
}