package com.example.BuildingManagement.room.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber; // e.g., "101" or "A-2"

    @NotNull(message = "Floor number is required")
    private Integer floorNo;
}