package com.example.BuildingManagement.power.Service;

import com.example.BuildingManagement.device.IotDevice;
import com.example.BuildingManagement.device.IotDeviceRepo;
import com.example.BuildingManagement.power.Model.Power;
import com.example.BuildingManagement.power.PowerMetric;
import com.example.BuildingManagement.power.Repository.PowerMetricRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PowerMetricService {

    private final PowerMetricRepo powerMetricRepo;
    private final IotDeviceRepo iotDeviceRepo;

    /**
     * Save incoming ESP telemetry data as a PowerMetric entity.
     * Looks up the IotDevice by the ESP's nodeId (= device_serial).
     */
    public PowerMetric saveMetric(Power telemetry) {
        Optional<IotDevice> deviceOpt = iotDeviceRepo.findByDeviceSerial(telemetry.getNodeId());

        if (deviceOpt.isEmpty()) {
            System.err.println("[PowerMetricService] No IoT device found for nodeId: " + telemetry.getNodeId());
            return null;
        }

        IotDevice device = deviceOpt.get();

        PowerMetric metric = new PowerMetric();
        metric.setIotDevice(device);
        metric.setVoltage(BigDecimal.valueOf(telemetry.getVoltage()));
        metric.setAmperes(BigDecimal.valueOf(telemetry.getCurrent()));
        metric.setWattage(BigDecimal.valueOf(telemetry.getPower()));
        metric.setUnitsConsumedTotal(BigDecimal.valueOf(telemetry.getEnergy()));
        metric.setRecordedAt(LocalDateTime.now());

        return powerMetricRepo.save(metric);
    }

    /**
     * Get the latest power reading for a room.
     */
    public PowerMetric getLatestReading(Long roomId) {
        IotDevice device = iotDeviceRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("No IoT device found for room: " + roomId));

        return powerMetricRepo.findTopByIotDeviceIdOrderByRecordedAtDesc(device.getId())
                .orElse(null);
    }

    /**
     * Get historical metrics for a room within a date range.
     */
    public List<PowerMetric> getMetricsByRoomAndDateRange(Long roomId, LocalDate startDate, LocalDate endDate) {
        IotDevice device = iotDeviceRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("No IoT device found for room: " + roomId));

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return powerMetricRepo.findByIotDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                device.getId(), start, end);
    }

    /**
     * Get cumulative energy usage for the current month for a room.
     * Returns a map with: unitsConsumed, startReading, latestReading, days
     */
    public Map<String, Object> getCumulativeUsage(Long roomId) {
        IotDevice device = iotDeviceRepo.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("No IoT device found for room: " + roomId));

        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now();

        // Get first reading of the month
        Optional<PowerMetric> firstReading = powerMetricRepo
                .findFirstByIotDeviceIdAndRecordedAtAfterOrderByRecordedAtAsc(
                        device.getId(), monthStart.minusSeconds(1));

        // Get latest reading
        Optional<PowerMetric> latestReading = powerMetricRepo
                .findTopByIotDeviceIdOrderByRecordedAtDesc(device.getId());

        Map<String, Object> usage = new HashMap<>();

        if (firstReading.isPresent() && latestReading.isPresent()) {
            BigDecimal startUnits = firstReading.get().getUnitsConsumedTotal();
            BigDecimal endUnits = latestReading.get().getUnitsConsumedTotal();
            BigDecimal consumed = endUnits.subtract(startUnits);

            usage.put("unitsConsumed", consumed);
            usage.put("startReading", startUnits);
            usage.put("latestReading", endUnits);
            usage.put("unitRate", device.getUnitRatePerKwh());
            usage.put("estimatedCost", consumed.multiply(
                    device.getUnitRatePerKwh() != null ? device.getUnitRatePerKwh() : BigDecimal.ZERO));
            usage.put("monthStart", monthStart);
            usage.put("currentTime", monthEnd);
        } else {
            usage.put("unitsConsumed", BigDecimal.ZERO);
            usage.put("startReading", BigDecimal.ZERO);
            usage.put("latestReading", BigDecimal.ZERO);
            usage.put("unitRate", device.getUnitRatePerKwh());
            usage.put("estimatedCost", BigDecimal.ZERO);
            usage.put("monthStart", monthStart);
            usage.put("currentTime", monthEnd);
        }

        usage.put("roomId", roomId);
        usage.put("deviceSerial", device.getDeviceSerial());

        return usage;
    }
}
