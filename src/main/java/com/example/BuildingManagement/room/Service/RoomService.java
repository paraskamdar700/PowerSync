package com.example.BuildingManagement.room.Service;

import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.room.Model.RoomRequest;
import com.example.BuildingManagement.user.Model.User;

import java.util.List;
public interface RoomService {
    Room addRoom(Long apartmentId, RoomRequest roomRequest, User landlord);
    List<Room> getRoomsByApartment(Long apartmentId, User landlord);
    Room getRoomById(Long apartmentId, Long roomId, User landlord);
    Room updateRoom(Long apartmentId, Long roomId, RoomRequest roomRequest, User landlord); // Added
    void deleteRoom(Long apartmentId, Long roomId, User landlord);
}