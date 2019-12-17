package com.github.livelocation;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LiveLocation extends LiveData<Location> {

    private static LiveLocation instance;

    private final FusedLocationProviderClient fusedLocationProviderClient;

    public static LiveLocation get(@NonNull Context context) {
        if (instance == null) {
            synchronized (LiveLocation.class) {
                instance = new LiveLocation(context.getApplicationContext());
            }
        }
        return instance;
    }

    private LiveLocation(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        getLastKnownLocation();
    }

    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setValue(location);
                        }
                    }
                });
    }
}
