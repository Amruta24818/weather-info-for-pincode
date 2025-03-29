package com.example.weather_info_for_pincode.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pincode;
    private LocalDate date;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode weatherData;

    public WeatherInfo() {}
    public WeatherInfo(String pincode, LocalDate date, JsonNode weatherData) {
        this.pincode = pincode;
        this.date = date;
        this.weatherData = weatherData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public JsonNode getWeatherData() {
        return weatherData;
    }

    public void setWeatherData(JsonNode weatherData) {
        this.weatherData = weatherData;
    }

}
