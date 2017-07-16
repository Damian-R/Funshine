package com.example.damia.funshine.activities;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.damia.funshine.R;
import com.example.damia.funshine.adapters.WeatherAdapter;
import com.example.damia.funshine.model.DailyWeatherReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Damian Reiter on 7/12/2017.
 */


public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // URL constants
    final String URL_BASE_FIVEDAY = "http://api.openweathermap.org/data/2.5/forecast/daily";
    final String URL_BASE_CURRENT = "http://api.openweathermap.org/data/2.5/weather";
    final String URL_COORD = "?lat=";
    final String URL_UNIT = "&units=metric";
    final String URL_API = "&APPID=d5530f3630141aa3166965d97fe73c46";
    final String URL_COUNT = "&cnt=6";

    // permission constant
    private final int PERMISSION_LOCATION = 123;

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<DailyWeatherReport> weatherReportArrayList = new ArrayList<>();

    // View initialization
    private ImageView weatherIcon;
    private ImageView weatherIconMini;
    private TextView weatherDate;
    private TextView currentTemp;
    private TextView lowTemp;
    private TextView cityCountry;
    private TextView weatherType;

    WeatherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.damia.funshine.R.layout.activity_weather);

        //building the GoogleApiClient with LocationServices API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .build();


        weatherIcon = (ImageView)findViewById(R.id.weather_icon);
        weatherIconMini = (ImageView)findViewById(R.id.weather_icon_mini);
        weatherDate = (TextView)findViewById(R.id.weather_date);
        currentTemp = (TextView)findViewById(R.id.currrent_temp);
        lowTemp = (TextView)findViewById(R.id.low_temp);
        cityCountry = (TextView)findViewById(R.id.city_country);
        weatherType = (TextView)findViewById(R.id.weather_type);

        //initializing adapter and recyclerView
        adapter = new WeatherAdapter(weatherReportArrayList);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
    }

    // Method to get current weather data from openweathermap weather API
    public void downloadCurrentWeatherData(Location location){
        // build URL String using the current weather base URL
        final String fullCords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE_CURRENT + fullCords + URL_UNIT + URL_API;
        Log.v("CURRENT", url);

        // create a GET JsonObjectRequest using Volley
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("FUN", response.toString());
                try {
                    // get all relevant weather information from JSON objects created from the web request
                    JSONArray weatherData = response.getJSONArray("weather");
                    JSONObject weather = weatherData.getJSONObject(0);
                    String weatherType = weather.getString("main");
                    String weatherDesc = weather.getString("description");
                    JSONObject main = response.getJSONObject("main");
                    Double currentTemp = main.getDouble("temp");
                    Double minTemp = main.getDouble("temp_min");

                    Log.v("TEMPS", "current: " + currentTemp.toString() + " min: " + minTemp.toString());

                    String city = response.getString("name");
                    JSONObject sys = response.getJSONObject("sys");
                    String country = sys.getString("country");

                    Log.v("CURRENT", city + " " + country);

                    // create new DailyWeatherReport for all current weather information
                    DailyWeatherReport currentReport = new DailyWeatherReport(city, country, currentTemp.intValue(), 0, minTemp.intValue(), weatherType, weatherDesc, new Date());

                    // update the UI for all of the current weather information
                    updateCurrentUI(currentReport);

                }catch (JSONException exception){
                    Log.v("JSON", exception.getLocalizedMessage().toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        // add the JsonObjectRequest to the Volley request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public void downloadFiveDayWeatherData(Location location){
        // build URL string using the daily forecast URL base
        final String fullCords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE_FIVEDAY + fullCords + URL_UNIT + URL_COUNT + URL_API;

        // create a GET JsonObjectRequest for the weather data for the next five days
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("FUN", "Res: " + response.toString());

                try{
                    JSONArray list = response.getJSONArray("list");
                    // for loop to get weather data for next 5 days (starting at 1 to skip the current day since we already got more accurate current information from downloadCurrentWeatherData)
                    for(int i = 1; i < 6; i++){
                        // get all relevant weather information from JSON objects created from the web request
                        JSONObject obj = list.getJSONObject(i);
                        Date date = new Date((long)Integer.parseInt(obj.getString("dt"))*1000);
                        Log.v("DATE2", date.toString());

                        JSONObject temps = obj.getJSONObject("temp");
                        Double maxTemp = temps.getDouble("max");
                        Double minTemp = temps.getDouble("min");

                        JSONArray weatherArr = obj.getJSONArray("weather");
                        JSONObject weather = weatherArr.getJSONObject(0);
                        String weatherType = weather.getString("main");
                        String weatherDesc = weather.getString("description");

                        // create a new DailyWeatherReport for each day...
                        DailyWeatherReport report = new DailyWeatherReport(null, null, 0, maxTemp.intValue(), minTemp.intValue(), weatherType, weatherDesc, date);

                        // ... and add it to an ArrayList of DailyWeatherReports to be used in the RecyclerView
                        weatherReportArrayList.add(report);
                    }

                }catch(JSONException exception){
                    Log.v("JSON", exception.toString());
                }

                // notify the adapter that the data set has changed since we just got new information from a web request
                adapter.notifyDataSetChanged();

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

    public void updateCurrentUI(DailyWeatherReport report){
        Log.v("FUN", report.getWeather());
        // update images based on weather type
        switch (report.getWeather()){
            case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                Log.v("FUN", "clouds called");
                break;

            case DailyWeatherReport.WEATHER_TYPE_RAIN:
                weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                Log.v("FUN", "rain called");
                break;

            case DailyWeatherReport.WEATHER_TYPE_SNOW:
                weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                Log.v("FUN", "snow called");
                break;

            default:
                weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                Log.v("FUN", "clear called");
        }

        // set all views to contain the proper information
        currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "°");
        lowTemp.setText(Integer.toString(report.getMinTemp()) + "°");
        cityCountry.setText(report.getCityName() + ", " + report.getCountry());
        weatherType.setText(report.getWeatherDesc());
        weatherDate.setText("Today, " + report.getFormattedDate());

        currentTemp.setVisibility(View.VISIBLE);
        lowTemp.setVisibility(View.VISIBLE);
        cityCountry.setVisibility(View.VISIBLE);
        weatherType.setVisibility(View.VISIBLE);
        weatherIcon.setVisibility(View.VISIBLE);
    }

    @Override
    // called when GoogleApiClient has connected to Google services
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }

    @Override
    // called when GoogleApiClient has failed to connect to Google services
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    // called when user has changed location
    public void onLocationChanged(Location location) {
        downloadCurrentWeatherData(location);
        downloadFiveDayWeatherData(location);
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
