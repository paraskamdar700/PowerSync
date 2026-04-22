package com.example.BuildingManagement.device;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceControlService deviceControlService;

    @PostMapping("/{id}/on")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Map<String, String>> turnDeviceOn(@PathVariable Long id) {
        boolean success = deviceControlService.turnOn(id);
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Device turned ON"));
        } else {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to reach ESP32 or update status"));
        }
    }

    @PostMapping("/{id}/off")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Map<String, String>> turnDeviceOff(@PathVariable Long id) {
        boolean success = deviceControlService.turnOff(id);
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Device turned OFF"));
        } else {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to reach ESP32 or update status"));
        }
    }

    @PutMapping("/universal-unit-rate")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Map<String, String>> updateUniversalUnitRate(@RequestBody Map<String, java.math.BigDecimal> payload) {
        java.math.BigDecimal unitRate = payload.get("unitRate");
        if (unitRate == null || unitRate.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid unitRate is required"));
        }

        try {
            deviceControlService.updateUniversalUnitRate(unitRate);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Universal unit rate updated to " + unitRate));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to update universal unit rate: " + e.getMessage()));
        }
    }
}
