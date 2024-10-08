package com.example.myapplication

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
//If granted by the user, execute the necessary function
                    getLastLocation()
                } else {

                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//If it's not granted yet, then the rationale dialog will be brought up
        when {
            hasLocationPermission() -> getLastLocation()

//If it has, then the rationale dialog will be brought up
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher
                        .launch(ACCESS_FINE_LOCATION)
                }
            }

            else -> requestPermissionLauncher
                .launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(positiveAction: () -> Unit) {

        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    private fun getLastLocation() {
        fusedLocationProviderClient.lastLocation
//If successfully fetched the location information from the API
            .addOnSuccessListener { location: Location? ->
                location?.let {

                    val userLocation = LatLng(location.latitude,
                        location.longitude)
//Move camera to that location
                    updateMapLocation(userLocation)
//Add a marker at the location position
                    addMarkerAtLocation(userLocation, "You")
                }
            }
    }


    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().title(title)
            .position(location))
    }
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            location, 7f))
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



}
