package com.khemraj.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Map extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    SharedPreferences sharedPreferences;
    LocationListener locationListener;
    public void centerMapOnLocation(Location location, String title){
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());

        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1111,11111,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location");
                LatLng loc = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,12));
                mMap.clear();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if(intent.getIntExtra("name",0) == 0){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    centerMapOnLocation(location,"Your Location");
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
            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location");
                mMap.clear();
                LatLng loc = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,12));
            }

        }else {

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("name",1)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("name",1)).longitude);


            mMap.clear();
            centerMapOnLocation(placeLocation,MainActivity.list.get(intent.getIntExtra("name",1)));
            LatLng loc = new LatLng(placeLocation.getLatitude(),placeLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,12));

        }



    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
           try{
               List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
               if(addresses.size()>0 && addresses!= null){

                   if(addresses.get(0).getThoroughfare() != null){
                       if(addresses.get(0).getSubThoroughfare() != null){
                           address += addresses.get(0).getSubThoroughfare() + " ";
                       }
                       if(!addresses.get(0).getThoroughfare().equalsIgnoreCase("unnamed road")) {
                           address += addresses.get(0).getThoroughfare() + " ";
                       }
                   }

                   if(addresses.get(0).getLocality() != null){
                       address += addresses.get(0).getLocality() + " ";
                   }

                   if(addresses.get(0).getSubAdminArea() != null){
                        address += addresses.get(0).getSubAdminArea();
                   }


               }

           }catch (Exception e){
               e.printStackTrace();
           }

           if(address.equals("")){
               SimpleDateFormat sdf = new  SimpleDateFormat("HH:mm:ss");
               address += sdf.format(new Date());
           }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

           MainActivity.list.add(address);
           MainActivity.locations.add(latLng);
           MainActivity.arrayAdapter.notifyDataSetChanged();

           sharedPreferences = this.getSharedPreferences("com.khemraj.memorableplaces",Context.MODE_PRIVATE);

        try {

            ArrayList<String> lattitudes = new ArrayList<String>();
            ArrayList<String> longitudes = new ArrayList<String>();

            for(int i=0;i<MainActivity.locations.size();i++){
                lattitudes.add(Double.toString(MainActivity.locations.get(i).latitude));
                longitudes.add(Double.toString(MainActivity.locations.get(i).longitude));
            }

            sharedPreferences.edit().putString("location",ObjectSerializer.serialize(MainActivity.list)).apply();
            sharedPreferences.edit().putString("lattitude",ObjectSerializer.serialize(lattitudes)).apply();
            sharedPreferences.edit().putString("longitude",ObjectSerializer.serialize(longitudes)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(),"Location added to your Favourites",Toast.LENGTH_SHORT).show();


    }
}
