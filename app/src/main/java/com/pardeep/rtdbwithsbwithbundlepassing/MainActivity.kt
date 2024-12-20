package com.pardeep.rtdbwithsbwithbundlepassing

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pardeep.rtdbwithsbwithbundlepassing.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding?.Next?.setOnClickListener {
            startActivity(Intent(this,MainActivity2::class.java))
            finish()
        }
        if (checkPermission()) {
            getLastLocation()
        } else {
            requestPermission()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {
                var latitude = location.latitude
                var longnitude = location.longitude
                var location = location
                binding?.locationTv?.setText("$location")
                binding?.lognitudeTv?.setText("$longnitude")
                binding?.latitudeTV?.setText("$latitude")

                findAddress(latitude, longnitude)
            }

        }
    }

    private fun findAddress(latitude: Double, longnitude: Double): String {
        val geocode = Geocoder(this, Locale.getDefault())
        try {
            val address = geocode.getFromLocation(latitude, longnitude, 1)
            if (address != null && address.isNotEmpty()) {
                val address = address[0]
                val addressString = address.getAddressLine(0)

                val placeIndex = addressString.indexOf(" ")
                if (placeIndex > -1) {
                    return addressString.substring(placeIndex + 1)
                } else {
                    return addressString
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "address not found"
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION , android.Manifest.permission.ACCESS_COARSE_LOCATION),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            100 ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastLocation()
                }else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}