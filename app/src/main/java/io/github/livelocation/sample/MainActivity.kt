package io.github.livelocation.sample

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.snackbar.Snackbar
import io.github.livelocation.ErrorType
import io.github.livelocation.LiveLocation
import io.github.livelocation.LocationError
import io.github.livelocation.LocationObserver

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parent: View = findViewById(R.id.container)

        LiveLocation.get(this).requestLocationUpdates()
            .observe(this, object : LocationObserver() {
                override fun onSuccess(location: Location) {
                }

                override fun onFailure(error: LocationError) {
                    when (error.type) {
                        ErrorType.PERMISSIONS_REQUIRED -> showPermissionSnackbar(parent)

                        ErrorType.SETTINGS_CHANGE_REQUIRED -> showSettingsSnackbar(parent,
                            error.exception() as ResolvableApiException)

                        //else -> Log.i("TEST", error.type.toString())
                    }
                }
            })


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        LiveLocation.get(this).onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LiveLocation.get(this).onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showPermissionSnackbar(view: View) {
        Snackbar.make(view, R.string.permission_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getText(R.string.action_title)) {
                LiveLocation.get(this).requestPermissions(this)
            }
            .show()
    }

    fun showSettingsSnackbar(view: View, resolvableApiException: ResolvableApiException) {
        Snackbar.make(view, R.string.settings_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getText(R.string.action_title)) {
                resolvableApiException
                    .startResolutionForResult(this, LiveLocation.REQUEST_CHECK_SETTINGS)
            }
            .show()
    }


}
