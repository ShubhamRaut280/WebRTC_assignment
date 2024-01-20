package com.shubham.webrtc_videochat.utils;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.google.firebase.auth.FirebaseUser;

public class helperFunctions {

    public boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                return true;
            }
        }

        return false;
    }

    public boolean isGPSEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void turnOnGPS(Context context) {
        Intent enableGps = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(enableGps);

    }



}
