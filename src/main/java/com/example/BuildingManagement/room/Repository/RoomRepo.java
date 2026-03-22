package com.example.BuildingManagement.room.Repository;

import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.user.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepo extends JpaRepository<Room, Long> {

    // Find all rooms belonging to a specific landlord
    List<Room> findByApartmentLandlordId(Long landlordId);

    // Find a room specifically by its tenant (useful for the tenant dashboard)
    Optional<Room> findByCurrentTenant(User tenant);

    // Find all vacant rooms in an apartment
    List<Room> findByApartmentIdAndCurrentTenantIsNull(Long apartmentId);

    Optional<Room> findByRoomNumber(Long roomNumber);

    List<Room> findByApartment(Apartment apartment);
}