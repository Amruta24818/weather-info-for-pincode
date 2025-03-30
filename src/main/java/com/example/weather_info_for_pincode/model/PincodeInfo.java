package com.example.weather_info_for_pincode.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PincodeInfo {

    @Id
    private String pincode;
    private Double latitude;
    private Double longitude;

    private String country;
    private String city;

    public PincodeInfo() {}

    public PincodeInfo(String pincode, Double latitude, Double longitude, String country, String city) {
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
