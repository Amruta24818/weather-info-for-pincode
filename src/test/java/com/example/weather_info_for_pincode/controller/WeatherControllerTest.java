package com.example.weather_info_for_pincode.controller;

import com.example.weather_info_for_pincode.model.PincodeInfo;
import com.example.weather_info_for_pincode.model.WeatherInfo;
import com.example.weather_info_for_pincode.service.IWeatherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class WeatherControllerTest {

    private final String pincode = "123456";
    private final String date = "2025-03-29";
    private final LocalDate requestedDate = LocalDate.parse(date);
    @Mock
    private IWeatherService weatherService;
    @InjectMocks
    private WeatherController weatherController;
    private WeatherInfo weatherInfo;
    private PincodeInfo pincodeInfo;
    private JsonNode weatherData;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        ObjectMapper objectMapper = new ObjectMapper();
        weatherData = objectMapper.readTree("{\"temp\": 72.5, \"description\": \"Sunny\"}");

        weatherInfo = new WeatherInfo(pincode, requestedDate, weatherData);

        pincodeInfo = new PincodeInfo();
        pincodeInfo.setLatitude(40.7128);
        pincodeInfo.setLongitude(-74.0060);
    }

    @Test
    void testGetWeather_WhenWeatherExists() {
        when(weatherService.getExistingWeather(pincode, requestedDate)).thenReturn(Optional.of(weatherInfo));

        ResponseEntity<WeatherInfo> response = weatherController.getWeather(pincode, date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sunny", response.getBody().getWeatherData().get("description").asText());
    }

    @Test
    void testGetWeather_WhenWeatherNotFound_ButPincodeInfoExists() {
        when(weatherService.getExistingWeather(pincode, requestedDate)).thenReturn(Optional.empty());
        when(weatherService.getPincodeInfo(pincode)).thenReturn(Optional.of(pincodeInfo));
        when(weatherService.fetchAndSaveWeather(pincode, requestedDate, pincodeInfo.getLatitude(), pincodeInfo.getLongitude()))
                .thenReturn(weatherInfo);

        ResponseEntity<WeatherInfo> response = weatherController.getWeather(pincode, date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sunny", response.getBody().getWeatherData().get("description").asText());
    }

    @Test
    void testGetWeather_WhenNoPincodeInfoFound() {
        when(weatherService.getExistingWeather(pincode, requestedDate)).thenReturn(Optional.empty());
        when(weatherService.getPincodeInfo(pincode)).thenReturn(Optional.empty());
        when(weatherService.fetchAndSavePincodeInfo(pincode)).thenReturn(Optional.empty());

        ResponseEntity<WeatherInfo> response = weatherController.getWeather(pincode, date);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetWeather_WhenPincodeInfoExists_ButWeatherFetchFails() {
        when(weatherService.getExistingWeather(pincode, requestedDate)).thenReturn(Optional.empty());
        when(weatherService.getPincodeInfo(pincode)).thenReturn(Optional.of(pincodeInfo));
        when(weatherService.fetchAndSaveWeather(pincode, requestedDate, pincodeInfo.getLatitude(), pincodeInfo.getLongitude()))
                .thenReturn(null);

        ResponseEntity<WeatherInfo> response = weatherController.getWeather(pincode, date);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetWeather_WhenPincodeInfoExists_AndWeatherFetchIsSuccessful() {
        when(weatherService.getExistingWeather(pincode, requestedDate)).thenReturn(Optional.empty());

        when(weatherService.getPincodeInfo(pincode)).thenReturn(Optional.of(pincodeInfo));

        when(weatherService.fetchAndSaveWeather(pincode, requestedDate, pincodeInfo.getLatitude(), pincodeInfo.getLongitude()))
                .thenReturn(weatherInfo);

        ResponseEntity<WeatherInfo> response = weatherController.getWeather(pincode, date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(weatherInfo, response.getBody());
        assertEquals("Sunny", response.getBody().getWeatherData().get("description").asText());
    }

}
