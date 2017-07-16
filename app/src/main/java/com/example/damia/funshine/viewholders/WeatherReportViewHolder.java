package com.example.damia.funshine.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.damia.funshine.R;
import com.example.damia.funshine.activities.WeatherActivity;
import com.example.damia.funshine.model.DailyWeatherReport;

/**
 * Created by Damian Reiter on 7/16/2017.
 */

public class WeatherReportViewHolder extends RecyclerView.ViewHolder{

    private ImageView weatherIcon;
    private TextView weatherDate;
    private TextView weatherDescription;
    private TextView tempHigh;
    private TextView tempLow;

    public WeatherReportViewHolder(View itemView) {
        super(itemView);

        weatherIcon = (ImageView)itemView.findViewById(R.id.list_weather_icon);
        weatherDate = (TextView)itemView.findViewById(R.id.list_weather_day);
        weatherDescription = (TextView)itemView.findViewById(R.id.list_weather_desc);
        tempHigh = (TextView)itemView.findViewById(R.id.list_temp_max);
        tempLow = (TextView)itemView.findViewById(R.id.list_temp_min);
    }

    public void updateUI(DailyWeatherReport report){

        weatherDate.setText(report.getFormattedDate());
        weatherDescription.setText(report.getWeather());
        tempHigh.setText(Integer.toString(report.getMaxTemp()) + "°");
        tempLow.setText(Integer.toString(report.getMinTemp()) + "°");

        // update weather icon based on the weather type for that day
        switch (report.getWeather()){
            case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                weatherIcon.setImageDrawable(weatherIcon.getContext().getResources().getDrawable(R.drawable.cloudy_mini));
                Log.v("FUN", "clouds called");
                break;

            case DailyWeatherReport.WEATHER_TYPE_RAIN:
                weatherIcon.setImageDrawable(weatherIcon.getContext().getResources().getDrawable(R.drawable.rainy_mini));
                Log.v("FUN", "rain called");
                break;

            case DailyWeatherReport.WEATHER_TYPE_SNOW:
                weatherIcon.setImageDrawable(weatherIcon.getContext().getResources().getDrawable(R.drawable.snow_mini));
                Log.v("FUN", "snow called");
                break;

            default:
                weatherIcon.setImageDrawable(weatherIcon.getContext().getResources().getDrawable(R.drawable.sunny_mini));
                Log.v("FUN", "clear called");
        }
    }
}