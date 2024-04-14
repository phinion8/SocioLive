package com.phinion.sociolive;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.phinion.sociolive.databinding.ActivityLoginBinding;
import com.phinion.sociolive.databinding.LoadingDialogBinding;
import com.phinion.sociolive.model.User;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 3;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
//    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth =FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        LoadingDialogBinding loadingDialogBinding = LoadingDialogBinding.inflate(LayoutInflater.from(this));



//        dialog = new AlertDialog.Builder(this)
//                .setView(loadingDialogBinding.getRoot())
//                .setCancelable(false)
//                .create();
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signWithGoogle();

        binding.signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signWithGoogle();

                binding.signButton.setEnabled(false);


            }
        });


    }
    public void signWithGoogle(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        binding.signButton.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);


        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {


            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authWithGoogle(account.getIdToken());
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign failed", Toast.LENGTH_SHORT).show();
            binding.signButton.setEnabled(true);

        }
    }


    void authWithGoogle(String idToken){

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), "Unknown", 500);
                            database.getReference()
                                            .child("profile")
                                                    .child(user.getUid())
                                                            .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finishAffinity();
                                            }else {

                                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else {
                            binding.signButton.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Sign In Failed..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}