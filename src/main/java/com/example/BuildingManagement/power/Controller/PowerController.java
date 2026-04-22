package com.example.BuildingManagement.power.Controller;

import com.example.BuildingManagement.power.Model.Power;
import com.example.BuildingManagement.power.PowerMetric;
import com.example.BuildingManagement.power.Service.PowerMetricService;
import com.example.BuildingManagement.power.WebSocket.PowerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
public class PowerController {

    private final PowerWebSocketHandler powerWebSocketHandler;
    private final PowerMetricService powerMetricService;

    @Autowired
    public PowerController(PowerWebSocketHandler powerWebSocketHandler, PowerMetricService powerMetricService) {
        this.powerWebSocketHandler = powerWebSocketHandler;
        this.powerMetricService = powerMetricService;
    }

    /**
     * ESP Microcontroller sends telemetry data here.
     * Data is persisted to DB and broadcasted to WebSocket clients.
     */
    @PostMapping("/esp")
    public String receiveTelemetry(@RequestBody Power power) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("==============================================");
        System.out.println("[" + timestamp + "] ESP Telemetry Received");
        System.out.println("  Node ID  : " + power.getNodeId());
        System.out.println("  Voltage  : " + power.getVoltage() + " V");
        System.out.println("  Current  : " + power.getCurrent() + " A");
        System.out.println("  Power    : " + power.getPower() + " W");
        System.out.println("  Energy   : " + power.getEnergy() + " kWh");
        System.out.println("==============================================");

        // Persist to database
        powerMetricService.saveMetric(power);

        // Build JSON string for WebSocket broadcast
        String json = String.format(
                "{\"nodeId\":\"%s\",\"voltage\":%.2f,\"current\":%.2f,\"power\":%.2f,\"energy\":%.2f,\"timestamp\":\"%s\"}",
                power.getNodeId(),
                power.getVoltage(),
                power.getCurrent(),
                power.getPower(),
                power.getEnergy(),
                timestamp
        );

        // Broadcast to all connected WebSocket clients
        powerWebSocketHandler.broadcast(json);

        return "{\"status\":\"success\",\"message\":\"Telemetry received and broadcasted\"}";
    }

    /**
     * Get the latest power reading for a specific room.
     */
    @GetMapping("/api/v1/power/room/{roomId}/latest")
    public ResponseEntity<?> getLatestReading(@PathVariable Long roomId) {
        PowerMetric metric = powerMetricService.getLatestReading(roomId);
        if (metric == null) {
            return ResponseEntity.ok(Map.of("message", "No readings found for room " + roomId));
        }
        return ResponseEntity.ok(metric);
    }

    /**
     * Get historical power metrics for a room within a date range.
     * Query params: startDate (yyyy-MM-dd), endDate (yyyy-MM-dd)
     * Defaults to current month if not provided.
     */
    @GetMapping("/api/v1/power/room/{roomId}/history")
    public ResponseEntity<List<PowerMetric>> getHistory(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<PowerMetric> metrics = powerMetricService.getMetricsByRoomAndDateRange(roomId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get cumulative energy usage for the current month for a room.
     * Returns: unitsConsumed, startReading, latestReading, unitRate, estimatedCost
     */
    @GetMapping("/api/v1/power/room/{roomId}/usage")
    public ResponseEntity<Map<String, Object>> getCumulativeUsage(@PathVariable Long roomId) {
        Map<String, Object> usage = powerMetricService.getCumulativeUsage(roomId);
        return ResponseEntity.ok(usage);
    }
}
