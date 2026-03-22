package com.example.BuildingManagement.apartment.Controller;


import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.apartment.Model.ApartmentRequest;
import com.example.BuildingManagement.apartment.Service.ApartmentService;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties/apartments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LANDLORD')")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    public ResponseEntity<Apartment> createApartment(
            @Valid @RequestBody ApartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(apartmentService.createApartment(request, principal.getUser()));
    }

    @GetMapping("/my-apartments")
    public ResponseEntity<List<Apartment>> getMyApartments(
            @AuthenticationPrincipal UserPrincipal principal) {
        System.out.println("Current Landlord ID: " + principal.getUser().getId());
        // Extract the User entity from the authenticated principal
        return ResponseEntity.ok(apartmentService.getApartmentsByLandlord(principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Apartment> updateApartment(
            @PathVariable Long id,
            @Valid @RequestBody ApartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(apartmentService.updateApartment(id, request, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        apartmentService.deleteApartment(id, principal.getUser());
        return ResponseEntity.ok("Apartment and all associated rooms deleted successfully");
    }
}