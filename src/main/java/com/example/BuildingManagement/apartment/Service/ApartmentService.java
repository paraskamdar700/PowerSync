package com.example.BuildingManagement.apartment.Service;

import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.apartment.Model.ApartmentRequest;
import com.example.BuildingManagement.user.Model.User;

import java.util.List;

public interface ApartmentService {
    Apartment createApartment(ApartmentRequest request, User landlord);

    List<Apartment> getApartmentsByLandlord(User landlord);

    Apartment updateApartment(Long id, ApartmentRequest request, User landlord);
    void deleteApartment(Long id, User landlord);
}