package com.example.weather_info_for_pincode.dao;

import com.example.weather_info_for_pincode.model.PincodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PincodeRepository extends JpaRepository<PincodeInfo, String> {
}
