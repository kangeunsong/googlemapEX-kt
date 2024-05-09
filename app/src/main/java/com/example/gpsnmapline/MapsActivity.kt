package com.example.gpsnmapline

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: MyLocationCallBack

    private lateinit var polyLineOptions: PolylineOptions

    // 가장 먼저 호출 (액티비티가 처음 생성될 때 호출되는 생명주기 메서드)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // 맵 프래그먼트를 가져와 초기화
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 위치 초기화 메서드 호출
        locationInit()

        // PolylineOptions 초기화
        polyLineOptions=PolylineOptions()
            .width(5f)
            .color(Color.RED)
    }

    // 위치 서비스 초기화
    private fun locationInit(){
        fusedLocationProviderClient=FusedLocationProviderClient(this)
        locationCallback=MyLocationCallBack()
        locationRequest=LocationRequest()

        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=10000
        locationRequest.fastestInterval=5000
    }

    // 맵이 준비되면 호출되는 콜백 메서드
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 맵 위에 마커를 추가하고 카메라 세팅
        val cbnu = LatLng(36.6281877, 127.4563363)
        mMap.addMarker(MarkerOptions().position(cbnu).title("충북대학교"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cbnu))
    }

    // 앱이 재개될 때 호출되는 메서드 (액티비티가 사용자와 상호작용할 수 있는 상태가 될 때 호출되는 생명주기 메서드)
    override fun onResume(){
        super.onResume()

        // 위치 권한 체크 후, 권한이 없는 경우 권한 요청 다이얼로그를 보여줌
        permissionCheck(cancel={
            showPermissionInfoDialog()
        }, ok={
            addLocationListener()
        })
    }

    // 위치 업데이트를 요청하는 메서드
    private fun addLocationListener(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // 위치 업데이트를 수신하는 콜백 클래스
    inner class MyLocationCallBack:LocationCallback(){
        // 일정시간마다 현재 위치를 업데이트하고 새로운 위도와 경도를 처리
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val location = locationResult?.lastLocation

            location?.run {
                val latLng = LatLng(latitude, longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
                Log.d("MapsAcitivy", "위도: $latitude, 경도: $longitude")

                polyLineOptions.add(latLng)

                mMap.addPolyline(polyLineOptions)
            }
            /*
            if (ContextCompat.checkSelfPermission(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                location?.run {
                    val latLng = LatLng(latitude, longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
                    Log.d("MapsAcitivy", "위도: $latitude, 경도: $longitude")

                    polyLineOptions.add(latLng)

                    mMap.addPolyline(polyLineOptions)
                }
            } else {
                // 위치 권한이 없는 경우에 대한 처리를 여기에 추가하세요.
                // 예를 들어, 권한을 요청할 수 있습니다.
                // ActivityCompat.requestPermissions()를 사용하여 위치 권한을 요청할 수 있습니다.
            } */
        }
    }

    // ACCESS_FINE_LOCATION 권한 요청을 위한 상수
    private val REQUEST_ACCESS_FINE_LOCATION=1000

    // 위치 권한 체크 및 요청 다이얼로그 표시 메서드
    private fun permissionCheck(cancel: ()->Unit, ok: ()->Unit){
        if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                cancel()
            } else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
            }
        }else{
            ok()
        }
    }

    // 권한 요청 다이얼로그 표시 메서드
    private fun showPermissionInfoDialog(){
        AlertDialog.Builder(this)
            .setMessage("현재 위치 정보를 얻으려면 위치 권한이 필요합니다")
            .setPositiveButton("확인") { _, _ ->
                ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    // 권한 요청 결과 처리 메서드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            REQUEST_ACCESS_FINE_LOCATION->{
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // 권한 허용됨
                    addLocationListener()
                } else{
                    // 권한 거부
                    Log.d("MapsActivity", "권한 거부됨")
                }
                return
            }
        }
    }

    // 앱이 일시정지 될 때 호출되는 메서드 (다른 액티비티가 화면을 가릴 때 호출되는 생명주기 메서드)
    override fun onPause(){
        super.onPause()

        // 위치 업데이트 리스너 제거
        removeLocationListener()
    }

    // 위치 업데이트 리스너 제거 메서드
    private fun removeLocationListener(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // 언급되지 않은 생명주기 메서드
    // onStart(): 액티비티가 화면에 나타날 때 호출
    // onStop(): 액티비티가 화면에서 완전히 사라질 때 호출
    // onDestroy(): 액티비티가 소멸될 때 호출
}