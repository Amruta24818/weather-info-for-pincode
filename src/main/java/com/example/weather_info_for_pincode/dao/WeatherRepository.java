package com.example.weather_info_for_pincode.dao;

import com.example.weather_info_for_pincode.model.WeatherInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherInfo, Long> {
    Optional<WeatherInfo> findByPincodeAndDate(String pincode, LocalDate date);

}
