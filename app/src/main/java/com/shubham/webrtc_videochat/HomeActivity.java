package com.shubham.webrtc_videochat;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shubham.webrtc_videochat.auth.PhoneAuthentication;
import com.shubham.webrtc_videochat.databinding.ActivityHomeBinding;

import org.xml.sax.helpers.LocatorImpl;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    helperFunctions fun = new helperFunctions();
    private static final int PER_REQ_CODE = 1000;
    FirebaseAuth auth;
    SupportMapFragment supportMapFragment;
    ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setting custom toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user.getDisplayName().isEmpty())
            Toast.makeText(this, "Please update your profile first!!", Toast.LENGTH_SHORT).show();
        else {
            binding.username.setText(user.getDisplayName());
            binding.userphone.setText("Phone : " + user.getPhoneNumber());
        }

        binding.getLocation.setOnClickListener(view -> {
            if(fun.isConnectedToInternet(this))
            {
                getcurrentLocation();
            }else
                Toast.makeText(this, "Please connect to internet.", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user.getDisplayName().isEmpty())
            Toast.makeText(this, "Please update your profile first!!", Toast.LENGTH_SHORT).show();
        else {
            binding.username.setText(user.getDisplayName());
            binding.userphone.setText("Phone : " + user.getPhoneNumber());
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: Map is ready!");

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);

            googleMap.setOnMapClickListener(latLng -> {
                // Handle map click events if necessary
                Toast.makeText(this, "Map Clicked at: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void getcurrentLocation() {
        // checking if GPS is enabled
        if (fun.isGPSEnabled(this)) {

            // using  fusedlocationprovider to get location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION}, PER_REQ_CODE);
                return;
            }
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
                if(location != null)
                {
                    Log.d(TAG, "I got the location : "+ "here is lattitude : "+ location.getLatitude()+" longitude : "+ location.getLongitude());
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // showing location on map

                    supportMapFragment.getMapAsync(googleMap -> {
                        googleMap.clear(); // Clear previous markers
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
                        googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    });

                }


            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch location! please try again..", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error in fetching the location : "+ e+" MESSAGE : "+e.getMessage());
            });
        }
        else
        {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            fun.turnOnGPS(this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PER_REQ_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (fun.isGPSEnabled(this)) {

                    getcurrentLocation();

                }else {

                    fun.turnOnGPS(this);
                }
            }
            else
            {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PER_REQ_CODE);
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, PhoneAuthentication.class));
            finish();
        }
        else if(id==R.id.updateProfile)
        {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        }

        return true;
    }
}