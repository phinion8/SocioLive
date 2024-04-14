package com.phinion.socioliveadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phinion.socioliveadmin.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {



    ActivityMainBinding binding;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();


        databaseReference = database.getReference().child("profile").child("Fdwhugx8wDhkZ9zArbGOHcvS7mw1").child("coins");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                binding.userCoins.setText(snapshot.getValue() + "");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference = database.getReference().child("profile").child("Fdwhugx8wDhkZ9zArbGOHcvS7mw1").child("name");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                binding.profileName.setText(snapshot.getValue() + "");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int coins = Integer.parseInt(binding.coinsEdit.getText().toString());

                database.getReference().child("profile").child("Fdwhugx8wDhkZ9zArbGOHcvS7mw1").child("coins").setValue(coins);

            }
        });




    }
}