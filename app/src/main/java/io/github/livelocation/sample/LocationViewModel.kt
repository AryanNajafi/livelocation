package io.github.livelocation.sample

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.location.LocationRequest
import io.github.livelocation.LiveLocation
import io.github.livelocation.Result

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val liveLocation: LiveLocation = LiveLocation.get(application)

    private val locationRequest = MutableLiveData<LocationRequest>()

    val locationData: LiveData<Result> =
        Transformations.switchMap(locationRequest, liveLocation::requestLocationUpdates)

    fun setLocationRequest(request: LocationRequest) {
        locationRequest.value = request
    }

    fun startLocationUpdates() {
        locationRequest.value = if (locationRequest.value != null) {
            locationRequest.value
        } else {
            LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 2000
                fastestInterval = 1000
            }
        }
    }

    fun stopLocationUpdates() {
        liveLocation.removeLocationUpdates()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        liveLocation.onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                   grantResults: IntArray) {
        liveLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}