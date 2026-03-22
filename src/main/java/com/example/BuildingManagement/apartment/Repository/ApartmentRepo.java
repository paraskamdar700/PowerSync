package com.example.BuildingManagement.apartment.Repository;

import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.user.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepo extends JpaRepository<Apartment, Long> {

    List<Apartment> findByLandlord(User landlord);

    // Security check: Find by ID ONLY if it belongs to this landlord
    Optional<Apartment> findByIdAndLandlord(Long id, User landlord);
}

