package com.example.BuildingManagement.device;

import com.example.BuildingManagement.room.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IotDeviceRepo extends JpaRepository<IotDevice, Long> {

    Optional<IotDevice> findByDeviceSerial(String deviceSerial);

    Optional<IotDevice> findByRoom(Room room);

    Optional<IotDevice> findByRoomId(Long roomId);
}
