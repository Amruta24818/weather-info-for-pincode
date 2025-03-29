package com.example.weather_info_for_pincode.service;

import com.example.weather_info_for_pincode.dao.PincodeRepository;
import com.example.weather_info_for_pincode.dao.WeatherRepository;
import com.example.weather_info_for_pincode.model.PincodeInfo;
import com.example.weather_info_for_pincode.model.WeatherInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class WeatherService implements IWeatherService{

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private PincodeRepository pincodeRepository;


    @Value("${api.key}")
    private String GEOCODE_API_URL;

    @Value("${WEATHER_API_URL.key }")
    private String WEATHER_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<WeatherInfo> getExistingWeather(String pincode, LocalDate date) {
        return weatherRepository.findByPincodeAndDate(pincode, date);
    }

    public Optional<PincodeInfo> getPincodeInfo(String pincode) {
        return pincodeRepository.findById(pincode);
    }

    public Optional<PincodeInfo> fetchAndSavePincodeInfo(String pincode) {
        String url = String.format(GEOCODE_API_URL, pincode);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if(response.getBody()!=null) {
                // Access the JSON data
                String zip = jsonNode.get("zip").asText();
                Double lat = jsonNode.get("lat").asDouble();
                Double lon = jsonNode.get("lon").asDouble();
                String name = jsonNode.get("name").asText();
                String country = jsonNode.get("country").asText();
                PincodeInfo info = new PincodeInfo(zip, lat, lon, country, name);
                pincodeRepository.save(info);
                return Optional.of(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public WeatherInfo fetchAndSaveWeather(String pincode, LocalDate date, double lat, double lon) {
        try{
            LocalDateTime localDateTime = date.atStartOfDay();

            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();

            long unixTimestamp = instant.getEpochSecond();

            String url = String.format(WEATHER_API_URL, lat, lon, unixTimestamp);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println(response.getBody());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode weatherData = objectMapper.readTree(response.getBody());// Convert raw JSON string to JsonNode
            WeatherInfo weather = new WeatherInfo(pincode, date, weatherData);
            weatherRepository.save(weather);
            return weather;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
