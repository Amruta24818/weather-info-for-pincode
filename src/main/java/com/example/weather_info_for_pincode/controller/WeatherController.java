package com.example.weather_info_for_pincode.controller;

import com.example.weather_info_for_pincode.model.PincodeInfo;
import com.example.weather_info_for_pincode.model.WeatherInfo;
import com.example.weather_info_for_pincode.service.IWeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private IWeatherService weatherService;

    @GetMapping(value = "/{pincode}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherInfo> getWeather(@PathVariable String pincode, @PathVariable String date) {
        LocalDate requestedDate = LocalDate.parse(date);

        Optional<WeatherInfo> existingWeather = weatherService.getExistingWeather(pincode, requestedDate);
        if (existingWeather.isPresent()) {
            WeatherInfo weatherInfo = existingWeather.get();
            return new ResponseEntity<>(weatherInfo, HttpStatus.OK);
        }

        Optional<PincodeInfo> pincodeInfo = weatherService.getPincodeInfo(pincode);
        if (!pincodeInfo.isPresent()) {
            pincodeInfo = weatherService.fetchAndSavePincodeInfo(pincode);
            if (!pincodeInfo.isPresent()) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }

        PincodeInfo pincodeData = pincodeInfo.get();
        WeatherInfo weather = weatherService.fetchAndSaveWeather(pincode, requestedDate, pincodeData.getLatitude(), pincodeData.getLongitude());

        if (weather == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(weather, HttpStatus.OK);
    }
}
