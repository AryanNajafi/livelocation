package io.github.livelocation.sample

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.material.snackbar.Snackbar
import io.github.livelocation.ErrorType
import io.github.livelocation.LiveLocation
import io.github.livelocation.LocationError
import io.github.livelocation.LocationObserver

class ViewModelActivity : AppCompatActivity() {

    private lateinit var viewModel: LocationViewModel

    private val locationRequest: LocationRequest = LocationRequest()

    private lateinit var parent: View
    private lateinit var priorityView: View
    private lateinit var intervalView: View
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var locationData: TextView
    private lateinit var priorityDropdown: AutoCompleteTextView
    private lateinit var intervalDropdown: AutoCompleteTextView

    private var updateRequested: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_model)
        initViews()

        viewModel = ViewModelProviders.of(this, ViewModelProvider
            .AndroidViewModelFactory(application)).get(LocationViewModel::class.java)

        viewModel.locationData.observe(this, object : LocationObserver() {
            override fun onSuccess(location: Location) {
                locationData.text = getString(R.string.location_data_format)
                    .format(location.latitude, location.longitude, location.accuracy)
            }

            override fun onFailure(error: LocationError) {
                when (error.type) {
                    ErrorType.PERMISSIONS_REQUIRED -> showPermissionSnackbar()
                    ErrorType.SETTINGS_CHANGE_REQUIRED ->
                        showSettingsSnackbar(error.exception() as ResolvableApiException)
                    else -> {}
                }
            }
        })

        locationRequest.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 2000
            fastestInterval = 1000
        }

        startButton.setOnClickListener {
            viewModel.startLocationUpdates()
            updateRequested = true
            updateUi()
        }
        stopButton.setOnClickListener {
            viewModel.stopLocationUpdates()
            updateRequested = false
            updateUi()
        }

        priorityDropdown.setOnItemClickListener { _, _, position, _ ->
            locationRequest.priority = when (position) {
                0 -> LocationRequest.PRIORITY_HIGH_ACCURACY
                1 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                2 -> LocationRequest.PRIORITY_LOW_POWER
                3 -> LocationRequest.PRIORITY_NO_POWER
                else -> LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            viewModel.setLocationRequest(locationRequest)
        }

        intervalDropdown.setOnItemClickListener { _, _, position, _ ->
            locationRequest.interval = 1000L + position * 500
            locationRequest.fastestInterval = locationRequest.interval / 2
            viewModel.setLocationRequest(locationRequest)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showPermissionSnackbar() {
        Snackbar.make(parent, R.string.permission_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getText(R.string.action_title)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LiveLocation.REQUEST_LOCATION_PERMISSIONS)
            }
            .show()
    }

    private fun showSettingsSnackbar(apiException: ResolvableApiException) {
        Snackbar.make(parent, R.string.settings_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getText(R.string.action_title)) {
                try {
                    apiException.startResolutionForResult(this, LiveLocation.REQUEST_CHECK_SETTINGS)
                } catch (e: IntentSender.SendIntentException) {
                }
            }
            .show()
    }

    private fun initViews() {
        parent = findViewById(R.id.container)
        priorityView = findViewById(R.id.priority_parent)
        intervalView = findViewById(R.id.interval_parent)
        startButton = findViewById(R.id.view_start)
        stopButton = findViewById(R.id.view_stop)
        locationData = findViewById(R.id.view_location)
        priorityDropdown = findViewById(R.id.priority_dropdown)
        intervalDropdown = findViewById(R.id.interval_dropdown)

        val priorityArray = resources.getStringArray(R.array.priority_array)
        val intervalArray = resources.getStringArray(R.array.interval_array)

        val priorityAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, priorityArray)
        val intervalAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, intervalArray)

        priorityDropdown.setAdapter(priorityAdapter)
        intervalDropdown.setAdapter(intervalAdapter)

        priorityDropdown.setText(priorityArray[0], false)
        intervalDropdown.setText(intervalArray[2], false)
    }

    private fun updateUi() {
        startButton.isEnabled = !updateRequested
        stopButton.isEnabled = updateRequested
        priorityView.isEnabled = updateRequested
        intervalView.isEnabled = updateRequested
    }
}
