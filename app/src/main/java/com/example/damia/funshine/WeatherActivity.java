package com.example.damia.funshine;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORD = "?lat="; //"?lat=33.6691148&lon=72.9149377";
    final String URL_UNIT = "&units=metric";
    final String URL_API = "&APPID=d5530f3630141aa3166965d97fe73c46";

    private final int PERMISSION_LOCATION = 123;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .build();

    }

    public void downloadWeatherData(Location location){

        final String fullCords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();

        final String url = URL_BASE + fullCords + URL_UNIT + URL_API;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("FUN", "Res: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("FUN", "Err: " + error.getLocalizedMessage());
            }
        });
        Log.v("FUN", url);
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    public void startLocationServices(){
        try{
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        }catch(SecurityException e){
            Log.v("FUN", e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationServices();
                }else{
                    Log.v("FUN", "Cannot start services, permissions not granted");
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG);
                }
            }
        }
    }
}
