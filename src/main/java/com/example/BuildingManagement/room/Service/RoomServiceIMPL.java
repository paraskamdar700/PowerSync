package com.example.BuildingManagement.room.Service;

import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.apartment.Repository.ApartmentRepo;
import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.room.Model.RoomRequest;
import com.example.BuildingManagement.room.Repository.RoomRepo;
import com.example.BuildingManagement.user.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceIMPL implements RoomService {

    private final RoomRepo roomRepo;
    private final ApartmentRepo apartmentRepo;

    @Override
    @Transactional
    public Room addRoom(Long apartmentId, RoomRequest roomRequest, User landlord) {
        // Verify apartment ownership before adding a room
        Apartment apartment = apartmentRepo.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        if (!apartment.getLandlord().getId().equals(landlord.getId())) {
            throw new AccessDeniedException("Unauthorized: You do not own this apartment");
        }

        Room room = new Room();
        room.setRoomNumber(roomRequest.getRoomNumber());
        room.setFloorNo(roomRequest.getFloorNo());
        room.setApartment(apartment);
        return roomRepo.save(room);
    }

    @Override
    public List<Room> getRoomsByApartment(Long apartmentId, User landlord) {
        // Verification: Ensure landlord owns the apartment before listing its rooms
        Apartment apartment = apartmentRepo.findByIdAndLandlord(apartmentId, landlord)
                .orElseThrow(() -> new AccessDeniedException("Apartment not found or unauthorized"));

        return roomRepo.findByApartment(apartment);
    }

    @Override
    public Room getRoomById(Long apartmentId, Long roomId, User landlord) {
        return getVerifiedRoom(apartmentId, roomId, landlord);
    }

    @Override
    @Transactional
    public Room updateRoom(Long apartmentId, Long roomId, RoomRequest roomRequest, User landlord) {
        Room room = getVerifiedRoom(apartmentId, roomId, landlord);

        room.setRoomNumber(roomRequest.getRoomNumber());
        room.setFloorNo(roomRequest.getFloorNo());

        return roomRepo.save(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long apartmentId, Long roomId, User landlord) {
        Room room = getVerifiedRoom(apartmentId, roomId, landlord);
        roomRepo.delete(room);
    }

    /**
     * Private Helper: Validates that the Room exists, belongs to the Landlord,
     * AND belongs to the specific Apartment ID provided in the URL.
     */
    private Room getVerifiedRoom(Long apartmentId, Long roomId, User landlord) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check 1: Ownership
        if (!room.getApartment().getLandlord().getId().equals(landlord.getId())) {
            throw new AccessDeniedException("Unauthorized: You do not own this property");
        }

        // Check 2: Structural Integrity (The "Apartment Context" check)
        if (!room.getApartment().getId().equals(apartmentId)) {
            throw new IllegalArgumentException("Resource Mismatch: Room " + roomId +
                    " does not belong to Apartment " + apartmentId);
        }

        return room;
    }
}