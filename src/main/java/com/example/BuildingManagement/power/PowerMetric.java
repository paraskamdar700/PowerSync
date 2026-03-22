package com.example.BuildingManagement.power;


import com.example.BuildingManagement.device.IotDevice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "power_metrics")
@Getter
@Setter
public class PowerMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "iot_device_id", nullable = false)
    private IotDevice iotDevice;

    private BigDecimal voltage;
    private BigDecimal amperes;
    private BigDecimal wattage;

    @Column(name = "units_consumed_total")
    private BigDecimal unitsConsumedTotal;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();
}