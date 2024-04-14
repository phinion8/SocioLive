package com.phinion.sociolive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phinion.sociolive.databinding.ActivityProfileBinding;
import com.phinion.sociolive.databinding.LoadingDialogBinding;
import com.phinion.sociolive.model.User;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        LoadingDialogBinding loadingDialogBinding = LoadingDialogBinding.inflate(LayoutInflater.from(this));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(loadingDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        dialog.show();

        DatabaseReference databaseReference = database.getReference().child("profile").child(mAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(ProfileActivity.this)
                        .load(user.getProfile())
                        .fitCenter()
                        .into(binding.profilePhoto);
                binding.cityName.setText(user.getCity());
                binding.userName.setText(user.getName());
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.updateCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.updateCityButton.setEnabled(false);
                String city = binding.cityName.getText().toString();
                if (city.equals("")){
                    binding.updateCityButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Please enter your city name.", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseDatabase.getInstance().getReference().child("profile")
                            .child(mAuth.getUid())
                            .child("city")
                            .setValue(city)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    binding.updateCityButton.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, "City Updated Successfully.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        binding.logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}