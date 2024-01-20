package com.shubham.webrtc_videochat;

import static android.view.View.GONE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shubham.webrtc_videochat.databinding.ActivityProfileBinding;
import com.shubham.webrtc_videochat.utils.RoundedCornerTransformation;
import com.shubham.webrtc_videochat.utils.helperFunctions;
import com.squareup.picasso.Picasso;

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
            binding.profileupdateProgressbar.setVisibility(GONE);
            binding.profileupdateRelative.setVisibility(View.VISIBLE);
            String username = user.getDisplayName();
            binding.phonenumber.setText(user.getPhoneNumber());
            Picasso.get().load(user.getPhotoUrl()).transform(new RoundedCornerTransformation(100)).into(binding.profiledp);


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


        // Register the launcher for picking images from gallery
        final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        uploadImageToStorage(selectedImageUri);
                    }
                }
        );
        binding.imagePicker.setOnClickListener(view -> {
            // Create an intent for picking an image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            // Launch the image picker activity using the launcher
            imagePickerLauncher.launch(intent);
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

    private void uploadImageToStorage(Uri imageUri) {
        String imageName = "profile_image_" + FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(imageName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        updateProfileWithImage(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
    private void updateProfileWithImage(Uri imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(imageUrl)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();

                        //  display the updated profile image
                        Picasso.get().load(imageUrl).transform(new RoundedCornerTransformation(50)).into(binding.profiledp);
                    } else {
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}