package com.example.damia.funshine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damia.funshine.R;
import com.example.damia.funshine.model.DailyWeatherReport;
import com.example.damia.funshine.viewholders.WeatherReportViewHolder;

import java.util.ArrayList;

/**
 * Created by Damian Reiter on 7/16/2017.
 */

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