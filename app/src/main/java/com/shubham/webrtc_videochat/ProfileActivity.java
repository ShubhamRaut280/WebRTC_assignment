package com.shubham.webrtc_videochat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static androidx.core.content.ContextCompat.getSystemService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.shubham.webrtc_videochat.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    helperFunctions func = new helperFunctions();
    FirebaseAuth auth;
    ActivityProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        // setting custom toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // show previous details of user if any
        if(func.isConnectedToInternet(this))
        {
            String username = user.getDisplayName();
            binding.phonenumber.setText(user.getPhoneNumber());

            if(!username.isEmpty())
            {
                binding.username.setText(username);
            }
        }else
            Toast.makeText(this, "Please connect to internet!! unable to load your details..", Toast.LENGTH_SHORT).show();

        binding.back.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });

        //update profile
        binding.update.setOnClickListener(view -> {
            String name = binding.username.getText().toString();

            if(name.isEmpty() )
                Toast.makeText(this, "Please enter details correctly!!", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(this, "Updating your details....", Toast.LENGTH_SHORT).show();
                updateUserDetailsatoFirebase(name, user);
            }
        });


    }


    // updating details of user
    private void updateUserDetailsatoFirebase(String name, FirebaseUser user) {

        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        // update name and photo if required
        user.updateProfile(req).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Toast.makeText(this, "Profile updated successfully" , Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "updateUserDetailsatoFirebase: error in updating profile : "+ e.getMessage());
            Toast.makeText(this, "Can't update profile!!", Toast.LENGTH_SHORT).show();
        });


    }



}