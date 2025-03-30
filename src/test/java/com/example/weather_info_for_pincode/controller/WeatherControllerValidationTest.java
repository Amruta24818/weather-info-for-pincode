package com.example.weather_info_for_pincode.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class WeatherControllerValidationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testValidPincodeAndDate() throws Exception {
        mockMvc.perform(get("/weather/411014/2025-03-30"))
                .andExpect(status().isOk());
    }

    @Test
    public void testInvalidPincode() throws Exception {
        mockMvc.perform(get("/weather/12345/2025-03-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("getWeather.pincode: size must be between 6 and 6")));
    }

    @Test
    public void testInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/weather/123456/30-03-2025"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("getWeather.date: Date must be in the format yyyy-mm-dd"));
    }

    @Test
    public void testBlankPincode() throws Exception {
        mockMvc.perform(get("/weather/ /2025-03-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("getWeather.pincode: must not be blank")))
                .andExpect(jsonPath("$.message", containsString("getWeather.pincode: size must be between 6 and 6")));
    }

    @Test
    public void testBlankDate() throws Exception {
        mockMvc.perform(get("/weather/123456/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No static resource weather/123456."));
    }
}
