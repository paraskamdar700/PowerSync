package com.example.BuildingManagement.apartment.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Generates Getters, Setters, and ToString
public class ApartmentRequest {

    @NotBlank(message = "Apartment name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name; // e.g., "Skyline PG"

    @NotBlank(message = "Address is required")
    private String address; // Physical location of the building
}