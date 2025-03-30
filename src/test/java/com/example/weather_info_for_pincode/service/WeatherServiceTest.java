package com.example.weather_info_for_pincode.service;

import com.example.weather_info_for_pincode.config.RestTemplateConfig;
import com.example.weather_info_for_pincode.dao.PincodeRepository;
import com.example.weather_info_for_pincode.dao.WeatherRepository;
import com.example.weather_info_for_pincode.model.PincodeInfo;
import com.example.weather_info_for_pincode.model.WeatherInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = RestTemplateConfig.class)
class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private PincodeRepository pincodeRepository;

    @Mock
    private RestTemplate restTemplate;
    private final String GEOCODE_API_URL = "https://api.openweathermap.org/geo/1.0/zip?zip=%s,in&appid=f26ffc0a115e905e9efd6b9efb493cf4";
    private final String WEATHER_API_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/%s,%s/%s?key=F4E7XSQ24EUQGSS7E745FZFVL";

    @InjectMocks
    private WeatherService weatherService = new WeatherService(restTemplate);


    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(weatherService, "GEOCODE_API_URL", GEOCODE_API_URL);
        ReflectionTestUtils.setField(weatherService, "WEATHER_API_URL", WEATHER_API_URL);
    }

    @Test
    void testGetExistingWeather() {
        String pincode = "123456";
        LocalDate date = LocalDate.of(2025, 3, 22);
        WeatherInfo weatherInfo = new WeatherInfo(pincode, date, null);

        when(weatherRepository.findByPincodeAndDate(pincode, date)).thenReturn(Optional.of(weatherInfo));

        Optional<WeatherInfo> result = weatherService.getExistingWeather(pincode, date);

        assertTrue(result.isPresent());
        assertEquals(weatherInfo, result.get());

        verify(weatherRepository, times(1)).findByPincodeAndDate(pincode, date);
    }

    @Test
    void testGetPincodeInfo() {
        String pincode = "123456";
        PincodeInfo pincodeInfo = new PincodeInfo(pincode, 12.34, 56.78, "Country", "City");

        when(pincodeRepository.findById(pincode)).thenReturn(Optional.of(pincodeInfo));

        Optional<PincodeInfo> result = weatherService.getPincodeInfo(pincode);

        assertTrue(result.isPresent());
        assertEquals(pincodeInfo, result.get());

        verify(pincodeRepository, times(1)).findById(pincode);
    }

    @Test
    void testFetchAndSavePincodeInfo() throws URISyntaxException, JsonProcessingException {
        String pincode = "123456";
        String jsonResponse = "{\"zip\":\"123456\",\"lat\":12.34,\"lon\":56.78,\"name\":\"City\",\"country\":\"Country\"}";

        ResponseEntity<String> response = ResponseEntity.ok(jsonResponse);
        when(restTemplate.getForEntity("https://api.openweathermap.org/geo/1.0/zip?zip=123456,in&appid=f26ffc0a115e905e9efd6b9efb493cf4", String.class)).thenReturn(response);

        PincodeInfo expectedPincodeInfo = new PincodeInfo(pincode, 12.34, 56.78, "Country", "City");
        when(pincodeRepository.save(any(PincodeInfo.class))).thenReturn(expectedPincodeInfo);

        Optional<PincodeInfo> result = weatherService.fetchAndSavePincodeInfo(pincode);

        assertTrue(result.isPresent());
        PincodeInfo responsePincodeInfo = result.get();
        assertEquals(expectedPincodeInfo.getPincode(), responsePincodeInfo.getPincode());

        verify(pincodeRepository, times(1)).save(any(PincodeInfo.class));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testFetchAndSaveWeather() throws Exception {
        String pincode = "123456";
        LocalDate date = LocalDate.of(2025, 3, 29);
        double lat = 12.34;
        double lon = 56.78;

        String weatherJsonResponse = "{\"weather\":{\"temperature\":22}}";
        ResponseEntity<String> response = ResponseEntity.ok(weatherJsonResponse);
        when(restTemplate.getForEntity("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/12.34,56.78/1743206400?key=F4E7XSQ24EUQGSS7E745FZFVL", String.class)).thenReturn(response);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode weatherData = objectMapper.readTree(weatherJsonResponse);
        WeatherInfo expectedWeatherInfo = new WeatherInfo(pincode, date, weatherData);
        when(weatherRepository.save(any(WeatherInfo.class))).thenReturn(expectedWeatherInfo);

        WeatherInfo result = weatherService.fetchAndSaveWeather(pincode, date, lat, lon);

        assertNotNull(result);
        assertEquals(expectedWeatherInfo.getPincode(), result.getPincode());

        verify(weatherRepository, times(1)).save(any(WeatherInfo.class));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }
}