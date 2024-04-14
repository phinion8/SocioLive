package com.phinion.sociolive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phinion.sociolive.databinding.ActivityMainBinding;
import com.phinion.sociolive.databinding.ExitAppDialogBinding;
import com.phinion.sociolive.databinding.LoadingDialogBinding;
import com.phinion.sociolive.databinding.WarningDialogBinding;
import com.phinion.sociolive.model.User;

import java.util.HashMap;



public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    long coins = 0;
    int requestCode = 1;
    ExitAppDialogBinding exitAppDialogBinding;
    AlertDialog eDialog;


    String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);


        LoadingDialogBinding loadingDialogBinding = LoadingDialogBinding.inflate(LayoutInflater.from(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(loadingDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        dialog.show();

        exitAppDialogBinding = ExitAppDialogBinding.inflate(LayoutInflater.from(this));
        eDialog = new AlertDialog.Builder(this)
                .setView(exitAppDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        exitAppDialogBinding.yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        exitAppDialogBinding.noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               eDialog.dismiss();
            }
        });
        eDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


        HashMap<String, Boolean> onlineUsers = new HashMap<>();
        onlineUsers.put(mAuth.getUid() , true);

        database.getReference().child("noOfUsersOnline").child(mAuth.getUid())
                .setValue(onlineUsers);


        DatabaseReference databaseReference = database.getReference().child("profile").child(mAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(MainActivity.this)
                        .load(user.getProfile())
                        .fitCenter()
                        .into(binding.profilePhoto);
                coins = user.getCoins();
                binding.userCoins.setText("Coins: "+coins);
                dialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
        databaseReference1.child("noOfUsersOnline")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            binding.onlineUsers.setText(snapshot.getChildrenCount() + "");
                        }else {
                            binding.onlineUsers.setText("1");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.tresureChest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });


        binding.profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });


        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    if (coins >= 250) {
                        startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient coins, watch some video ads to earn some coins.", Toast.LENGTH_SHORT).show();
                    }
                    }else{
                    askPermissions();
                }
            }
        });









    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    public boolean isPermissionGranted(){
        for (String permission : permissions){
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.getReference().child("noOfUsersOnline").child(mAuth.getUid()).removeValue();
    }

    @Override
    protected void onStop() {
        super.onStop();

        database.getReference().child("noOfUsersOnline").child(mAuth.getUid()).removeValue();database.getReference().child("noOfUsersOnline").child(mAuth.getUid()).removeValue();

    }

    @Override
    public void onBackPressed() {
        eDialog.show();
    }
}