package com.example.BuildingManagement.apartment.Service;


import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.apartment.Model.ApartmentRequest;
import com.example.BuildingManagement.apartment.Repository.ApartmentRepo;
import com.example.BuildingManagement.user.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentServiceIMPL implements ApartmentService {

    private final ApartmentRepo apartmentRepo;

    @Override
    @Transactional
    public Apartment createApartment(ApartmentRequest request, User landlord) {
        Apartment apartment = new Apartment();
        apartment.setName(request.getName());
        apartment.setAddress(request.getAddress());
        apartment.setLandlord(landlord);
        return apartmentRepo.save(apartment);
    }

    @Override
    public List<Apartment> getApartmentsByLandlord(User landlord) {

        return apartmentRepo.findByLandlord(landlord);
    }

    @Override
    @Transactional
    public Apartment updateApartment(Long id, ApartmentRequest request, User landlord) {
        // Find by both ID and Landlord to ensure ownership
        Apartment apartment = apartmentRepo.findByIdAndLandlord(id, landlord)
                .orElseThrow(() -> new AccessDeniedException("Apartment not found or unauthorized"));

        apartment.setName(request.getName());
        apartment.setAddress(request.getAddress());

        return apartmentRepo.save(apartment);
    }

    @Override
    @Transactional
    public void deleteApartment(Long id, User landlord) {
        Apartment apartment = apartmentRepo.findByIdAndLandlord(id, landlord)
                .orElseThrow(() -> new AccessDeniedException("Apartment not found or unauthorized"));

        // This will also delete all Rooms due to CascadeType.ALL defined in the Entity
        apartmentRepo.delete(apartment);
    }
}