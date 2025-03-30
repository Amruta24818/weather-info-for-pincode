package com.example.weather_info_for_pincode.service;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
//        "GEOCODE_API_URL=https://api.openweathermap.org/geo/1.0/zip?zip=%s,in&appid=f26ffc0a115e905e9efd6b9efb493cf4"
//        "weather.api.url=https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/%s,%s/%s?key=F4E7XSQ24EUQGSS7E745FZFVL"
})
class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private PincodeRepository pincodeRepository;

    @Mock
    private RestTemplate restTemplate;

    @Value("${GEOCODE_API_URL}")
    private String GEOCODE_API_URL ;

    @Value("${WEATHER_API_URL}")
    private String WEATHER_API_URL;

    @InjectMocks
    private WeatherService weatherService;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGetExistingWeather() {
        String pincode = "12345";
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
        String pincode = "12345";
        PincodeInfo pincodeInfo = new PincodeInfo(pincode, 12.34, 56.78, "Country", "City");

        when(pincodeRepository.findById(pincode)).thenReturn(Optional.of(pincodeInfo));

        Optional<PincodeInfo> result = weatherService.getPincodeInfo(pincode);

        assertTrue(result.isPresent());
        assertEquals(pincodeInfo, result.get());

        verify(pincodeRepository, times(1)).findById(pincode);
    }

    @Test
    void testFetchAndSavePincodeInfo() throws URISyntaxException, JsonProcessingException {
        String pincode = "12345";
        String jsonResponse = "{\"zip\":\"12345\",\"lat\":12.34,\"lon\":56.78,\"name\":\"City\",\"country\":\"Country\"}";

        ResponseEntity<String> response = ResponseEntity.ok(jsonResponse);
//        when(restTemplate.getForEntity(startsWith("https://api.openweathermap.org/geo/1.0/zip?zip=%s,in&appid=f26ffc0a115e905e9efd6b9efb493cf4" + pincode), eq(String.class))).thenReturn(response);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.openweathermap.org/geo/1.0/zip?zip="+12345+",in&appid=f26ffc0a115e905e9efd6b9efb493cf4")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(jsonResponse))
                );

        PincodeInfo expectedPincodeInfo = new PincodeInfo(pincode, 12.34, 56.78, "Country", "City");
        when(pincodeRepository.save(any(PincodeInfo.class))).thenReturn(expectedPincodeInfo);

        Optional<PincodeInfo> result = weatherService.fetchAndSavePincodeInfo(pincode);

        assertTrue(result.isPresent());
        assertEquals(expectedPincodeInfo, result.get());

        verify(pincodeRepository, times(1)).save(any(PincodeInfo.class));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testFetchAndSaveWeather() throws Exception {
        String pincode = "12345";
        LocalDate date = LocalDate.of(2025, 3, 29);
        double lat = 12.34;
        double lon = 56.78;

        String weatherJsonResponse = "{\"weather\":{\"temperature\":22}}";
        ResponseEntity<String> response = ResponseEntity.ok(weatherJsonResponse);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(response);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode weatherData = objectMapper.readTree(weatherJsonResponse);
        WeatherInfo expectedWeatherInfo = new WeatherInfo(pincode, date, weatherData);
        when(weatherRepository.save(any(WeatherInfo.class))).thenReturn(expectedWeatherInfo);

        WeatherInfo result = weatherService.fetchAndSaveWeather(pincode, date, lat, lon);

        assertNotNull(result);
        assertEquals(expectedWeatherInfo, result);

        verify(weatherRepository, times(1)).save(any(WeatherInfo.class));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }
}

