package edu.niu.z1846838.parkcarapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import edu.niu.z1846838.parkcarapp.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ActivityMapsBinding binding;
    private Geocoder geocoder;
    private int ACCESS_LOCATION_REQUEST_CODE = 1001;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button mButton, mRemoveBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mRemoveBtn = findViewById(R.id.removebtn);
        mButton = findViewById(R.id.button);

        //If the user clicks on on button a marker will be placed on map
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

                locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                            if(addresses.size() > 0){        //We have an address
                                Address address = addresses.get(0);
                                String streetAddress = address.getAddressLine(0);   //Address of where the marker was dropped
                                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                        .title("Parked Car Location")   //Label on Marker
                                        .snippet(streetAddress)     //Address generated
                                        .draggable(true) //Allows marker to be dragged
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

                                //Toast message will appear to on screen with directions of how the can
                                //Manipulate the marker
                                Toast.makeText(MapsActivity.this, "Hold and Drag to Move Marker", Toast.LENGTH_LONG).show();

                                //Remove marker button will be visible to allow the user to remove marker
                                //From the screen and set a new one
                                mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        marker.remove();
                                        mButton.setVisibility(view.VISIBLE);
                                        mRemoveBtn.setVisibility(view.INVISIBLE);
                                    }
                                });
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

                mButton.setVisibility(view.INVISIBLE);
                mRemoveBtn.setVisibility(view.VISIBLE);
            }
        });
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);     //Type of map shown on screen
        mMap.setOnMarkerDragListener(this);     //Track where the marker was moved to

        //Adds Zoom in Zoom out feature on map
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        //Check if location permission has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            zoomToUserLocation();
        } else {
            //Ask for permission for location
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Show & ask for permission to user
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);
            }
        }

        //Type of map we want to see
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        //Find latitude and longitude of a place
        try {
            List<Address> addresses = geocoder.getFromLocationName("london", 1);

            Address address = addresses.get(0);
            Log.d(TAG, "onMapReady: " + address.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        mMap.setMyLocationEnabled(true);
    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override       //Tracking the current user latitude and longitude
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

            }
        });


    }


    //For dragging the markers location
    //This method will always be called whenever the marker is being moved
    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    //Whenever we have finished dragging our marker we need to get our new location using
    //this method (lat / long) / new address from the location
    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        Log.d(TAG,"onMarkerDragEnd: ");
        LatLng latLng = marker.getPosition();

        //Display address of locator
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses.size() > 0){
                //We have an address
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                marker.setTitle("Parked Car Location");
                marker.setSnippet(streetAddress);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Park on Marker drag methods, enables the start of marker movement
    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
    }
    //Permission method for user location on google map
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            } else {
                //Not granted

            }
        }
    }
}
