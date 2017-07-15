package com.example.damia.funshine.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by damia on 7/12/2017.
 */

public class DailyWeatherReport {

    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_SNOW = "Snow";

    private String cityName;
    private String country;
    private int currentTemp;
    private int maxTemp;

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    private int minTemp;
    private String weather;
    private String formattedDate;
    private String weatherDesc;

    public DailyWeatherReport(String cityName, String country, int currentTemp, int maxTemp, int minTemp, String weather, String weatherDesc, Date rawDate) {
        this.cityName = cityName;
        this.country = country;
        this.currentTemp = currentTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = weather;
        this.formattedDate = rawDateToFormatted(rawDate);
        this.weatherDesc = weatherDesc;
    }

    public String rawDateToFormatted(Date date){
        String month = new SimpleDateFormat("MMMM").format(date);
        String day = new SimpleDateFormat("dd").format(date);
        Log.v("DATE", date.toString());
        return month + " " + day;
    }
}
