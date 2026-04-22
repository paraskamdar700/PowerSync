package com.example.BuildingManagement.device;

import com.example.BuildingManagement.common.enums.DeviceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DeviceControlService {

    private final IotDeviceRepo iotDeviceRepo;

    /**
     * Send an HTTP GET request to the ESP32 server to turn ON the electricity.
     */
    public boolean turnOn(Long deviceId) {
        IotDevice device = iotDeviceRepo.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (device.getIpAddress() == null || device.getIpAddress().isEmpty()) {
            throw new RuntimeException("Device IP address is not configured");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + device.getIpAddress() + ":8081/on";
            
            // Assume the ESP32 returns 200 OK on success
            restTemplate.getForEntity(url, String.class);
            
            device.setStatus(DeviceStatus.ON);
            iotDeviceRepo.save(device);
            System.out.println("✅ Device " + device.getDeviceSerial() + " turned ON successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to turn ON device " + device.getDeviceSerial() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Send an HTTP GET request to the ESP32 server to turn OFF the electricity.
     */
    public boolean turnOff(Long deviceId) {
        IotDevice device = iotDeviceRepo.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (device.getIpAddress() == null || device.getIpAddress().isEmpty()) {
            throw new RuntimeException("Device IP address is not configured");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + device.getIpAddress() + ":8081/off";
            
            // Assume the ESP32 returns 200 OK on success
            restTemplate.getForEntity(url, String.class);
            
            device.setStatus(DeviceStatus.OFF);
            iotDeviceRepo.save(device);
            System.out.println("✅ Device " + device.getDeviceSerial() + " turned OFF successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to turn OFF device " + device.getDeviceSerial() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the unit rate globally for all registered IoT devices.
     */
    public void updateUniversalUnitRate(java.math.BigDecimal newRate) {
        java.util.List<IotDevice> allDevices = iotDeviceRepo.findAll();
        for (IotDevice device : allDevices) {
            device.setUnitRatePerKwh(newRate);
        }
        iotDeviceRepo.saveAll(allDevices);
        System.out.println("✅ Universal unit rate updated to " + newRate + " for " + allDevices.size() + " devices.");
    }
}
