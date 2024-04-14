package com.phinion.sociolive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phinion.sociolive.databinding.ActivityRewardBinding;
import com.phinion.sociolive.databinding.LoadingDialogBinding;
import com.phinion.sociolive.databinding.SuccessDialogBinding;
import com.phinion.sociolive.databinding.WarningDialogBinding;
import com.phinion.sociolive.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class RewardActivity extends AppCompatActivity {

    ActivityRewardBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    private RewardedAd mRewardedAd;
    private final String TAG = "MainActivity";
    AlertDialog dialog, wDialog, sDialog;
    SuccessDialogBinding successDialogBinding;
    LoadingDialogBinding loadingDialogBinding;
    WarningDialogBinding warningDialogBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //Loading Dialog
        loadingDialogBinding = LoadingDialogBinding.inflate(LayoutInflater.from(this));
        dialog = new AlertDialog.Builder(this)
                .setView(loadingDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        dialog.show();

        //Warning Dialog
        warningDialogBinding = WarningDialogBinding.inflate(LayoutInflater.from(this));
        AlertDialog wDialog = new AlertDialog.Builder(this)
                .setView(warningDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        warningDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wDialog.dismiss();
            }
        });
        wDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        //Success Dialog
        successDialogBinding = SuccessDialogBinding.inflate(LayoutInflater.from(this));
        sDialog = new AlertDialog.Builder(this)
                .setView(successDialogBinding.getRoot())
                .setCancelable(false)
                .create();
        successDialogBinding.okSuccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RewardActivity.this, MainActivity.class));
                finish();
            }
        });
        sDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


        //Loading Reward Video Ad
        loadAd();

        //Loading Coins
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                database.getReference().child("profile")
                        .child(mAuth.getUid()).child("coins").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                binding.coins.setText(snapshot.getValue() + "");
                                dialog.dismiss();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        }, 4000);


        binding.cardView100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                String todayString = year + "" + month + "" + day;

                SharedPreferences preferences = getSharedPreferences("PREF1", 0);
                boolean currentDay = preferences.getBoolean(todayString, false);




                if (!currentDay) {

                    if (mRewardedAd != null) {


                        Activity activityContext = RewardActivity.this;
                        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward
                                FirebaseDatabase.getInstance().getReference()
                                        .child("profile")
                                        .child(mAuth.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("profile")
                                                        .child(mAuth.getUid()).child("coins").setValue(user.getCoins() + 100).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.putBoolean(todayString, true);
                                                                editor.apply();
                                                                successDialogBinding.errorTitle.setText(100 + " Coins credited in your account successfully.");
                                                                sDialog.show();
                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });

                            }
                        });
                    } else {
                        dialog.dismiss();
                        loadAd();
                        Toast.makeText(RewardActivity.this, "The rewarded ad wasn't ready yet, loading ad...", Toast.LENGTH_SHORT).show();

                    }




                } else {

                    dialog.dismiss();
                    wDialog.show();

                }


            }


        });

        binding.cardView200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                String todayString = year + "" + month + "" + day;

                SharedPreferences preferences = getSharedPreferences("PREF2", 0);
                boolean currentDay = preferences.getBoolean(todayString, false);


                if (!currentDay) {

                    if (mRewardedAd != null) {



                        Activity activityContext = RewardActivity.this;
                        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward
                                FirebaseDatabase.getInstance().getReference()
                                        .child("profile")
                                        .child(mAuth.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("profile")
                                                        .child(mAuth.getUid()).child("coins").setValue(user.getCoins() + 200).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.putBoolean(todayString, true);
                                                                editor.apply();
                                                                successDialogBinding.errorTitle.setText(200 + " Coins credited in your account successfully.");
                                                                sDialog.show();

                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });

                            }
                        });
                    } else {
                        dialog.dismiss();
                        loadAd();
                        Toast.makeText(RewardActivity.this, "The rewarded ad wasn't ready yet, loading ad...", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    dialog.dismiss();
                    wDialog.show();

                }


            }


        });

        binding.cardView300.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                String todayString = year + "" + month + "" + day;

                SharedPreferences preferences = getSharedPreferences("PREF3", 0);
                boolean currentDay = preferences.getBoolean(todayString, false);


                if (!currentDay) {

                    if (mRewardedAd != null) {


                        Activity activityContext = RewardActivity.this;
                        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward
                                FirebaseDatabase.getInstance().getReference()
                                        .child("profile")
                                        .child(mAuth.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("profile")
                                                        .child(mAuth.getUid()).child("coins").setValue(user.getCoins() + 300).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.putBoolean(todayString, true);
                                                                editor.apply();
                                                                successDialogBinding.errorTitle.setText(300 + " Coins credited in your account successfully.");
                                                                sDialog.show();
                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });

                            }
                        });
                    } else {
                        dialog.dismiss();
                        loadAd();
                        Toast.makeText(RewardActivity.this, "The rewarded ad wasn't ready yet, loading ads...", Toast.LENGTH_SHORT).show();

                    }




                } else {

                    dialog.dismiss();
                    wDialog.show();

                }


            }


        });

        binding.cardView400.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();


                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                String todayString = year + "" + month + "" + day;

                SharedPreferences preferences = getSharedPreferences("PREF4", 0);
                boolean currentDay = preferences.getBoolean(todayString, false);


                if (!currentDay) {

                    if (mRewardedAd != null) {


                        Activity activityContext = RewardActivity.this;
                        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                // Handle the reward
                                FirebaseDatabase.getInstance().getReference()
                                        .child("profile")
                                        .child(mAuth.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("profile")
                                                        .child(mAuth.getUid()).child("coins").setValue(user.getCoins() + 400).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                editor.putBoolean(todayString, true);
                                                                editor.apply();
                                                                successDialogBinding.errorTitle.setText(400 + " Coins credited in your account successfully.");
                                                                sDialog.show();
                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });

                            }
                        });
                    } else {
                        dialog.dismiss();
                        loadAd();
                        Toast.makeText(RewardActivity.this, "The rewarded ad wasn't ready yet, loading ads...", Toast.LENGTH_SHORT).show();

                    }




                } else {

                    dialog.dismiss();
                    wDialog.show();

                }


            }


        });

    }


    public void loadAd() {
        dialog.show();
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-1596995520497352/9063200014",
                adRequest, new RewardedAdLoadCallback() {

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Toast.makeText(RewardActivity.this, "Failed to load the ad, please try again.", Toast.LENGTH_SHORT).show();
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        dialog.dismiss();
                        Toast.makeText(RewardActivity.this, "Ad loaded...", Toast.LENGTH_SHORT).show();
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                    }
                });


    }


}
