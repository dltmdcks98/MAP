package com.example.map;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.IOException;
import java.util.List;

//구글 맵 연동 https://webnautes.tistory.com/647 , https://webnautes.tistory.com/1249
//현재 위치 조회  https://wonpaper.tistory.com/230 , https://gwynn.tistory.com/4 , https://github.com/wonderful-coding-life/sample-location/tree/master/app/src/main/java/com/sample/location
public class MainActivity extends AppCompatActivity
        implements AutoPermissionsListener, OnMapReadyCallback {
    GoogleMap map;
    //위치 조회를 위함
    LocationManager manager;
    GPSListener gpsListener;
    SupportMapFragment mapFragment;
    Location location;


    //DB조회
    private DatabaseReference mDatabase;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("map");
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new GPSListener();

        try {
            MapsInitializer.initialize(this);

        } catch (Exception e) {
//            e.printStackTrace();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocationService();
        //AutoPermission 승인 처리
        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    public void startLocationService() {
        try {
            long minTime = 0;
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
//                manager.removeUpdates(gpsListener);

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
                Toast("네트워크 이용");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }




    class GPSListener implements LocationListener {

        // 위치 확인되었을때 자동으로 호출됨 (일정시간 and 일정거리)
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
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
//https://webnautes.tistory.com/1249
//https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=zoipower&logNo=30160106099
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
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map = googleMap;
        map.setMyLocationEnabled(true);

        //기본 marker
        //        1.https://gun0912.tistory.com/57
        //        2.https://fjdkslvn.tistory.com/17
        //        3.https://steemit.com/kr-dev/@gbgg/firebase-3-firebase
        //마커 클러스터 https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering

        ClusterManager<MyItem> mclusterManager = new ClusterManager<>(this, map);
        map.setOnCameraIdleListener(mclusterManager);
        map.setOnMarkerClickListener(mclusterManager);

        //지오코딩 https://bitsoul.tistory.com/135
        Geocoder geocoder = new Geocoder(this);


        //map에 DB 내용 추가
//       firestore
//        https://firebase.google.com/docs/firestore/query-data/get-data?hl=ko
//        db.collection("Marker")
//                .whereEqualTo("title",true)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if(task.isSuccessful()){
//                            for(QueryDocumentSnapshot document : task.getResult()){
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        }else{
//                            Log.w("Error","Error getting document");
//                        }
//                    }
//                });



        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int i = 1; i <= snapshot.getChildrenCount(); i++){
                    mDatabase.child(String.valueOf(i)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            MapDB map = snapshot.getValue(MapDB.class);
//                            double latitude = map.getLatitude();
//                            double longitude = map.getLongitude();
                            List<Address> list = null;
                            String title = map.getTitle();
                            try{
                                list = geocoder.getFromLocationName(title,10);
                            }catch (IOException e ){
                                e.printStackTrace();
                                Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
                            }
//지오 코딩
                            if(list != null){
                                if(list.size() == 0 ){
                                    Toast("해당 주소가 없습니다.");
                                }else{
                                    Address address = list.get(0);
                                    double latitude = address.getLatitude();
                                    double longitude = address.getLongitude();
                                    mclusterManager.addItem(new MyItem(latitude, longitude, title));
                                }
                            }
//                            for(int i =0; i< 10; i++) {
//                                mclusterManager.addItem(new MyItem(latitude, longitude, title));
//                                latitude +=1;
//                                longitude +=1;
//                                title +=1;
//                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

//클러스터 아이템 클릭시 이벤트 발생
        mclusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {
                Toast("test");
                return false;
            }
        });

        //클러스터 클릭시 클러스터 확대 및 시점 이동
        //https://1d1cblog.tistory.com/119
        mclusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                LatLng latLng = new LatLng(cluster.getPosition().latitude, cluster.getPosition().longitude);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                map.moveCamera(cameraUpdate);
                return false;
             }
        });


    }

    //https://github.com/lakue119/FirebaseSample/blob/master/app/src/main/java/com/lakue/firebasesample/MainActivity.java

    //지오 코딩 : https://blog.naver.com/PostView.nhn?isHttpsRedirect=true&blogId=qbxlvnf11&logNo=221183308547&parentCategoryNo=&categoryNo=44&viewDate=&isShowPopularPosts=false&from=postView

    public  void Toast(String str){
        Toast myToast = Toast.makeText(this.getApplicationContext(),str, Toast.LENGTH_SHORT);
        myToast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
    @Override
    public void onGranted(int requestCode, String[] permissions) {
    }
    @Override
    public void onDenied(int requestCode, String[] permissions) {
    }
    
}