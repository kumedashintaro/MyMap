package com.example.mymap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var locationCallback: LocationCallback? = null
    private lateinit var realm: Realm
    private lateinit var mMarker: GoogleMap


    //メニュー表示させる
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //画面をスリープにしない
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        realm = Realm.getDefaultInstance()

        memoBtn.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("lat", lastLocation.latitude)
            intent.putExtra("lng", lastLocation.longitude)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (::mMap.isInitialized) {
            putsMarkers()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkPermission()
    }

    override fun onPause() {
        super.onPause()
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            myLocationEnable()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            //許可を求め、拒否されていた場合
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            //まだ許可を求めていない
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION -> {
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //許可された
                    myLocationEnable()
                } else {
                    showToast("現在位置は表示できません")
                }
            }
        }
    }

    private fun myLocationEnable() {
        //赤い波線でエラーが表示されてしまうので
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            val locationRequest = LocationRequest().apply {
                interval = 10000            //最も長い時間
                fastestInterval = 5000        //最も短い時間
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult?.lastLocation != null) {
                        lastLocation = locationResult.lastLocation
                        val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        textView.text =
                            "Lat:${lastLocation.latitude}, Lng:${lastLocation.longitude}"
                    }
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
            putsMarkers()
        }

    }

    private fun putsMarkers() {

        mMap.clear()

        val realmResult = realm.where(Memo::class.java)
            .findAll()
        for (memo: Memo in realmResult) {
            val latLng = LatLng(memo.lat, memo.lng)
            val marker = MarkerOptions()
                .position(latLng) //場所
                .title(DateFormat.format("yyyy/MM/dd kk:mm", memo.dateTime).toString())
                .snippet(memo.memo)
                .draggable(false)  //マーカーはドラッグ不可

            //マーカーの外観
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val stringValue = pref.getString("markercolor", "")

            if (stringValue == "red") {
                val descriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                marker.icon(descriptor)
            } else if (stringValue == "blue") {
                val descriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                marker.icon(descriptor)
            } else if (stringValue == "green") {
                val descriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                marker.icon(descriptor)
            }

            //マーカー追加
            mMap.addMarker(marker)

        }
    }


    private fun showToast(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.list -> {
                val intent = Intent(this, ListActivity::class.java)
                startActivity(intent)
            }
            R.id.markersetting -> {
                val intent = Intent(this, MarkerSetActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
