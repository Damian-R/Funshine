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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    final String URL_BASE_FIVEDAY = "http://api.openweathermap.org/data/2.5/forecast/daily";
    final String URL_BASE_CURRENT = "http://api.openweathermap.org/data/2.5/weather";
    final String URL_COORD = "?lat="; //"?lat=33.6691148&lon=72.9149377";
    final String URL_UNIT = "&units=metric";
    final String URL_API = "&APPID=d5530f3630141aa3166965d97fe73c46";
    final String URL_COUNT = "&cnt=6";

    private final int PERMISSION_LOCATION = 123;

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<DailyWeatherReport> weatherReportArrayList = new ArrayList<>();

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
        setContentView(R.layout.activity_weather);

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

        adapter = new WeatherAdapter(weatherReportArrayList);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));

    }

    public void downloadCurrentWeatherData(Location location){
        final String fullCords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE_CURRENT + fullCords + URL_UNIT + URL_API;
        Log.v("CURRENT", url);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("FUN", response.toString());
                try {
                    JSONArray weatherData = response.getJSONArray("weather");
                    JSONObject weather = weatherData.getJSONObject(0);
                    String weatherType = weather.getString("main");
                    String weatherDesc = weather.getString("description");
                    String weatherID = weather.getString("id");

                    Log.v("CURRENT", weatherID);

                    JSONObject main = response.getJSONObject("main");
                    Double currentTemp = main.getDouble("temp");
                    Double minTemp = main.getDouble("temp_min");

                    Log.v("TEMPS", "current: " + currentTemp.toString() + " min: " + minTemp.toString());

                    String city = response.getString("name");
                    JSONObject sys = response.getJSONObject("sys");
                    String country = sys.getString("country");

                    Log.v("CURRENT", city + " " + country);
                    DailyWeatherReport currentReport = new DailyWeatherReport(city, country, currentTemp.intValue(), 0, minTemp.intValue(), weatherType, weatherDesc, new Date());

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

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public void downloadFiveDayWeatherData(Location location){
        final String fullCords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE_FIVEDAY + fullCords + URL_UNIT + URL_COUNT + URL_API;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("FUN", "Res: " + response.toString());

                try{
                    JSONArray list = response.getJSONArray("list");
                    // for loop to get weather data for next 5 days
                    for(int i = 1; i < 6; i++){
                        JSONObject obj = list.getJSONObject(i);
                        Date date = new Date((long)Integer.parseInt(obj.getString("dt"))*1000);
                        Log.v("DATE2", date.toString());

                        JSONObject temps = obj.getJSONObject("temp");
                        Double maxTemp = temps.getDouble("max");
                        Double minTemp = temps.getDouble("min");

                        //Log.v("TEMP", maxTemp.toString());

                        JSONArray weatherArr = obj.getJSONArray("weather");
                        JSONObject weather = weatherArr.getJSONObject(0);
                        String weatherType = weather.getString("main");
                        String weatherDesc = weather.getString("description");

                        DailyWeatherReport report = new DailyWeatherReport(null, null, 0, maxTemp.intValue(), minTemp.intValue(), weatherType, weatherDesc, date);

                        weatherReportArrayList.add(report);
                    }

                }catch(JSONException exception){
                    Log.v("JSON", exception.toString());
                }

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
        Log.v("FUN", DailyWeatherReport.WEATHER_TYPE_RAIN);
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
        currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "°");
        lowTemp.setText(Integer.toString(report.getMinTemp()) + "°");
        cityCountry.setText(report.getCityName() + ", " + report.getCountry());
        weatherType.setText(report.getWeatherDesc());
        weatherDate.setText("Today, " + report.getFormattedDate());
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

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {
        private ArrayList<DailyWeatherReport> dailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> dailyWeatherReports) {
            this.dailyWeatherReports = dailyWeatherReports;
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = dailyWeatherReports.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return dailyWeatherReports.size();
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder{

        private ImageView lweatherIcon;
        private TextView lweatherDate;
        private TextView lweatherDescription;
        private TextView ltempHigh;
        private TextView ltempLow;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            lweatherIcon = (ImageView)itemView.findViewById(R.id.list_weather_icon);
            lweatherDate = (TextView)itemView.findViewById(R.id.list_weather_day);
            lweatherDescription = (TextView)itemView.findViewById(R.id.list_weather_desc);
            ltempHigh = (TextView)itemView.findViewById(R.id.list_temp_max);
            ltempLow = (TextView)itemView.findViewById(R.id.list_temp_min);

        }

        public void updateUI(DailyWeatherReport report){

            lweatherDate.setText(report.getFormattedDate());
            lweatherDescription.setText(report.getWeather());
            ltempHigh.setText(Integer.toString(report.getMaxTemp()) + "°");
            ltempLow.setText(Integer.toString(report.getMinTemp()) + "°");

            switch (report.getWeather()){
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    Log.v("FUN", "clouds called");
                    break;

                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    Log.v("FUN", "rain called");
                    break;

                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow_mini));
                    Log.v("FUN", "snow called");
                    break;

                default:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    Log.v("FUN", "clear called");
            }
        }
    }
}
