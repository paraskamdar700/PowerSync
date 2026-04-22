package com.example.BuildingManagement.power.Repository;

import com.example.BuildingManagement.power.PowerMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PowerMetricRepo extends JpaRepository<PowerMetric, Long> {

    // Fetch all metrics for a device within a date range
    List<PowerMetric> findByIotDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            Long deviceId, LocalDateTime start, LocalDateTime end);

    // Get the most recent reading for a device
    Optional<PowerMetric> findTopByIotDeviceIdOrderByRecordedAtDesc(Long deviceId);

    // Get the first reading after a given date (for bill start - tenant move-in)
    Optional<PowerMetric> findFirstByIotDeviceIdAndRecordedAtAfterOrderByRecordedAtAsc(
            Long deviceId, LocalDateTime after);

    // Get the last reading before a given date (for bill end)
    Optional<PowerMetric> findFirstByIotDeviceIdAndRecordedAtBeforeOrderByRecordedAtDesc(
            Long deviceId, LocalDateTime before);

    // Get all metrics for a device ordered by time
    List<PowerMetric> findByIotDeviceIdOrderByRecordedAtDesc(Long deviceId);
}
