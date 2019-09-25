package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    protected static HashSet<String> totalBusNos = new HashSet<>();
    protected static Boolean filterCalled = false;
    protected static Boolean firstTime = true;
    protected static HashSet<String> filteredBusNo;
    private Bitmap iconMarker;

    // check if there is any saved state during oncreate.
    // initialize the floating filter button
    // set button click listener to the filter button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setupMapIfNeeded();
        FloatingActionButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                MultiSelectDialog dialogBox = new MultiSelectDialog(formatBusNos());
                dialogBox.show(fragmentManager, "tagSeleccion");
            }
        });
    }

    // this method converts the hashSet containg the bus number into String array
    private String[] formatBusNos() {
        Object[] arr = totalBusNos.toArray();
        String[] busNoString = new String[arr.length];
        System.out.println("The array is:");
        for (int j = 0; j < arr.length; j++)
            busNoString[j] = (String) arr[j];
        return busNoString;
    }

    // checks if the map need to be initialized again
    private void setupMapIfNeeded() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    // once the google map is ready, move the camera to previous location
    // if opening the app for first time, then camera will take you to current location after giving location permission
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkLocationPermission()) {
            MapStateManager mgr = new MapStateManager(this);
            CameraPosition position = mgr.getSavedCameraPosition();

            if (position != null) {
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                mMap.moveCamera(update);
                mMap.setMapType(mgr.getSavedMapType());
            }
            initMap();
            resizeMarkerIcon();
            runTimeInterval();

        }
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);

            mMap.setMapType(mgr.getSavedMapType());
        }
    }

    //  this method is called in oncreate method
    //  this method resizes the bus marker icon
    private void resizeMarkerIcon() {
        int height = 120;
        int width = 120;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bus, null);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        iconMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    // this method is called when opening the app
    // sets current location options in google maps
    private void initMap() {
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);

        }
    }

    // this method returns the current location of the user
    private LatLng getCurrentLocation() {
        double latitude = 0, longitude = 0;
        LatLng latLng = new LatLng(latitude, longitude);
        if (checkLocationPermission()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            latLng = new LatLng(latitude, longitude);
        }
        return latLng;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    // during onPause method, the current state of the map is saved
    @Override
    protected void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);
    }

    // calls the async task every 15 seconds
    private void runTimeInterval() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                new VehiclePositionReader(mMap, iconMarker).execute();
            }
        }, 0, 15000);
    }

    // this method checks for the checking and getting user permission for location
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).setTitle(R.string.title_location_permission).setMessage(R.string.text_location_permission).setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                }).create().show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission is given
                // initialize the map, initialize timer, zoom to current position
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                    resizeMarkerIcon();
                    runTimeInterval();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getCurrentLocation(), 12.0f));
                }

            }
        }
    }


}
