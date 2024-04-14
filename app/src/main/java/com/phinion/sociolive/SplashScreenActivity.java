package com.phinion.sociolive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mAuth.getCurrentUser() != null){
                   startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                }else {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                }
                finish();


            }
        }, 1500);
    }


}