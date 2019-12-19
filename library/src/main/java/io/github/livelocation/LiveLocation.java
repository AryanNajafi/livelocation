package io.github.livelocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LiveLocation extends LiveData<Result> {

    public static final int REQUEST_CHECK_SETTINGS = 9000;
    public static final int REQUEST_LOCATION_PERMISSIONS = 9001;

    private static final long UPDATE_INTERVAL = 4000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    private static LiveLocation instance;

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final SettingsClient settingsClient;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    private boolean updatesRequested;

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
        settingsClient = LocationServices.getSettingsClient(context);
        getLastKnownLocation();
    }

    public LiveLocation requestLocationUpdates() {
        updatesRequested = true;
        createLocationCallback();
        if (locationRequest == null) {
            createLocationRequest();
        }
        createLocationSettingsRequest();
        return this;
    }

    public LiveLocation requestLocationUpdates(LocationRequest request) {
        locationRequest = request;
        return requestLocationUpdates();
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (updatesRequested) {
            checkLocationUpdates();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (updatesRequested) {
            stopLocationUpdates();
        }
    }

    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setValue(Result.success(location));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof SecurityException) {
                            if (!updatesRequested) {
                                setValue(Result.failure(ErrorType.PERMISSIONS_REQUIRED));
                            }
                        } else if (e instanceof ApiException) {
                            setValue(Result.failure(ErrorType.LOCATION_API));
                        } else {
                            setValue(Result.failure(ErrorType.UNKNOWN));
                        }
                    }
                });
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    setValue(Result.success(locationResult.getLastLocation()));
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (!locationAvailability.isLocationAvailable()) {
                    checkLocationUpdates();
                }
            }
        };
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof SecurityException) {
                            setValue(Result.failure(ErrorType.PERMISSIONS_REQUIRED));
                        }
                    }
                });
    }

    private void checkLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        startLocationUpdates();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                ResolvableApiException rae = (ResolvableApiException) e;
                                setValue(Result.failure(ErrorType.SETTINGS_CHANGE_REQUIRED, rae));
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                setValue(Result.failure(ErrorType.SETTINGS_CHANGE_UNAVAILABLE));
                                break;

                            default:
                                break;
                        }
                    }
                });
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void removeLocationUpdates() {
        if (updatesRequested) {
            stopLocationUpdates();
            updatesRequested = false;
        }
    }

    public void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSIONS);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                if (updatesRequested) {
                    startLocationUpdates();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                setValue(Result.failure(ErrorType.SETTINGS_CHANGE_DENIED));
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0) {
                checkPermissionsResult(grantResults[0]);
            }
        } else {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    checkPermissionsResult(grantResults[i]);
                    break;
                }
            }
        }
    }

    private void checkPermissionsResult(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
            if (updatesRequested) {
                checkLocationUpdates();
            }
        } else {
            setValue(Result.failure(ErrorType.PERMISSIONS_DENIED));
        }
    }
}
