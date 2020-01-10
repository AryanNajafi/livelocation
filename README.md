# LiveLocation
LiveLocation is a library to simplify the usage of the Android fused location provider along with Android location permissions, device location settings and Activity lifecycle. 

## Download
```groovy
implementation 'io.github:livelocation:1.0.0-alpha01'
```
## Using LiveLocation

### Get the last known location

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LiveLocation.get(this).lastLocation.observe(this, object : LocationObserver() {
            override fun onSuccess(location: Location) {
                // Location object
            }

            override fun onFailure(error: LocationError) {
                // Error
            }
        })
    }
}
```

### Receive periodic location updates

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val request = LocationRequest.create()?.apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        LiveLocation.get(this).requestLocationUpdates(request)
            .observe(this, object : LocationObserver() {
                override fun onSuccess(location: Location) {
                    // Receive location updates
                }

                override fun onFailure(error: LocationError) {
                    // Error
                }
            })
    }
}
```

### Location failure
```kotlin
//..

override fun onFailure(error: LocationError) {
    when (error.type) {
        ErrorType.PERMISSIONS_REQUIRED ->
            // Granting location permissions is required
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LiveLocation.REQUEST_LOCATION_PERMISSIONS)

        ErrorType.PERMISSIONS_DENIED ->
            // Granting location permissions is denied by user

        ErrorType.SETTINGS_CHANGE_REQUIRED -> {
            // Changing/Enabling location settings is required
            val rae = error.exception() as ResolvableApiException
            rae.startResolutionForResult(this@MainActivity, LiveLocation.REQUEST_CHECK_SETTINGS)
        }
            
        ErrorType.SETTINGS_CHANGE_DENIED ->
            // Changing location settings is denied by user

        ErrorType.SETTINGS_CHANGE_UNAVAILABLE ->
            // Can't change location settings

        ErrorType.LOCATION_API ->
            // Google Play services failure
    }
}
```

### Deliver permissions and settings results
```kotlin
class MainActivity : AppCompatActivity() {
    
    // Permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        LiveLocation.get(this).onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
    // Location settings change result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LiveLocation.get(this).onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```
