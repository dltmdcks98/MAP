package com.example.map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.HashMap;

//구글 맵 연동 https://webnautes.tistory.com/647
//현재 위치 조회  https://wonpaper.tistory.com/230
public class MainActivity extends AppCompatActivity
        implements AutoPermissionsListener, OnMapReadyCallback {
    GoogleMap map;
    //위치 조회를 위함
    LocationManager manager;
    GPSListener gpsListener;
    SupportMapFragment mapFragment;

    //DB조회
    private DatabaseReference mDatabase;
    Double latitude;
    Double longitude;

    private Button DB_Button, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("map");

        readUser();

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new GPSListener();
        try {
            MapsInitializer.initialize(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        DB_Button = (Button) findViewById(R.id.button1);
        location = (Button) findViewById(R.id.button2);


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
            }
        });
        DB_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readUser();
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    public void startLocationService() {
        try {
            Location location = null;

            long minTime = 0;        // 0초마다 갱신 - 바로바로갱신
            float minDistance = 0;

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String message = "최근 위치1 -> Latitude : " + latitude + "\n Longitude : " + longitude;
                    showCurrentLocation(latitude, longitude);
                    Log.i("MyLocTest", "최근 위치1 호출" + message);
                }

                //위치 요청하기
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);
                //manager.removeUpdates(gpsListener);
                Toast.makeText(getApplicationContext(), "내 위치1확인 요청함", Toast.LENGTH_SHORT).show();
                Log.i("MyLocTest", "requestLocationUpdates() 내 위치1에서 호출시작 ~~ ");

            } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String message = "최근 위치2 -> Latitude : " + latitude + "\n Longitude : " + longitude;
                    showCurrentLocation(latitude, longitude);
                    Log.i("MyLocTest", "최근 위치2 호출" + message);
                }

                //위치 요청하기
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = googleMap;
        map.setMyLocationEnabled(true);

        //경도, 위도
        mDatabase.child("map").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                com.example.map.Location group = snapshot.getValue(com.example.map.Location.class);
                latitude = group.getLatitude();
                longitude = group.getLongitude();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LatLng SEOUL = new LatLng(latitude, longitude);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Toast(marker.getTitle());
                return false;
            }
        });
        //TODO : DB 데이터로 marker만들기
//        1.https://gun0912.tistory.com/57
//        2.https://fjdkslvn.tistory.com/17
//        3.https://steemit.com/kr-dev/@gbgg/firebase-3-firebase
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
    }


    //https://github.com/lakue119/FirebaseSample/blob/master/app/src/main/java/com/lakue/firebasesample/MainActivity.java

    //DB 읽기
    private void readUser() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.getValue(com.example.map.Location.class) != null) {
                    com.example.map.Location post = dataSnapshot.getValue(com.example.map.Location.class);
                    Log.w("FireBaseData", "getData" + post.toString());
                } else {
                    Toast.makeText(MainActivity.this, "데이터 없음...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    class GPSListener implements LocationListener {

        // 위치 확인되었을때 자동으로 호출됨 (일정시간 and 일정거리)
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String message = "내 위치는 Latitude : " + latitude + "\nLongtitude : " + longitude;
            showCurrentLocation(latitude, longitude);
            Log.i("MyLocTest", "onLocationChanged() 호출되었습니다.");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        // GPS provider를 이용전에 퍼미션 체크
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "접근 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
                //manager.removeUpdates(gpsListener);
            } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpsListener);
                //manager.removeUpdates(gpsListener);
            }

            if (map != null) {
                map.setMyLocationEnabled(true);
            }
            Log.i("MyLocTest", "onResume에서 requestLocationUpdates() 되었습니다.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeUpdates(gpsListener);

        if (map != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(false);
        }
        Log.i("MyLocTest","onPause에서 removeUpdates() 되었습니다.");
    }

    private void showCurrentLocation(double latitude, double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
//        showMyLocationMarker(curPoint);
    }

//    private void showMyLocationMarker(LatLng curPoint) {
//        if (myLocationMarker == null) {
//            myLocationMarker = new MarkerOptions(); // 마커 객체 생성
//            myLocationMarker.position(curPoint);
//            myLocationMarker.title("최근위치 \n");
//            myLocationMarker.snippet("*GPS로 확인한 최근위치");
//            myLocationMarker.icon(BitmapDescriptorFactory.fromResource((R.drawable.mylocation)));
//            myMarker = map.addMarker(myLocationMarker);
//        } else {
//            myMarker.remove(); // 마커삭제
//            myLocationMarker.position(curPoint);
//            myMarker = map.addMarker(myLocationMarker);
//        }
//
//        // 반경추가
//        if (circle1KM == null) {
//            circle1KM = new CircleOptions().center(curPoint) // 원점
//                    .radius(1000)       // 반지름 단위 : m
//                    .strokeWidth(1.0f);    // 선너비 0f : 선없음
//            //.fillColor(Color.parseColor("#1AFFFFFF")); // 배경색
//            circle = map.addCircle(circle1KM);
//
//        } else {
//            circle.remove(); // 반경삭제
//            circle1KM.center(curPoint);
//            circle = map.addCircle(circle1KM);
//        }
//
//
//    }

    public  void Toast(String str){
        Toast myToast = Toast.makeText(this.getApplicationContext(),str, Toast.LENGTH_SHORT);
        myToast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
        Toast.makeText(this, "requestCode : "+requestCode+"  permissions : "+permissions+"  grantResults :"+grantResults, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onGranted(int requestCode, String[] permissions) {
        Toast.makeText(getApplicationContext(),"permissions granted : " + permissions.length, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(getApplicationContext(),"permissions denied : " + permissions.length, Toast.LENGTH_SHORT).show();
    }



}