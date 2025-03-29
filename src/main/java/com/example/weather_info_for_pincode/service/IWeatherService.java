package com.example.weather_info_for_pincode.service;

import com.example.weather_info_for_pincode.model.PincodeInfo;
import com.example.weather_info_for_pincode.model.WeatherInfo;

import java.time.LocalDate;
import java.util.Optional;

public interface IWeatherService {
    public WeatherInfo fetchAndSaveWeather(String pincode, LocalDate date, double lat, double lon);

    public Optional<PincodeInfo> fetchAndSavePincodeInfo(String pincode);

    public Optional<PincodeInfo> getPincodeInfo(String pincode);

    public Optional<WeatherInfo> getExistingWeather(String pincode, LocalDate date);
}

