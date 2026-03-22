package com.example.BuildingManagement.device;

import com.example.BuildingManagement.common.enums.DeviceStatus;
import com.example.BuildingManagement.room.Model.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "iot_devices")
@Getter @Setter
public class IotDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial", unique = true, nullable = false)
    private String deviceSerial;

    @OneToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status; // ON, OFF

    @Column(name = "unit_rate_per_kwh")
    private BigDecimal unitRatePerKwh;
}
