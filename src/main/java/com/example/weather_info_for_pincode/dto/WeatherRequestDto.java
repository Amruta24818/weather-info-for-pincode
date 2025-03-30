package com.example.weather_info_for_pincode.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WeatherRequestDto {

    @NotNull(message = "Pincode is required.")
    @Size(min = 6, max = 6, message = "Pincode must be exactly 6 digits.")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be numeric and 6 digits long.")
    private String pincode;

    @NotNull(message = "Date is required.")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format.")
    @PastOrPresent(message = "Date cannot be in the future.")
    private String date;

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
