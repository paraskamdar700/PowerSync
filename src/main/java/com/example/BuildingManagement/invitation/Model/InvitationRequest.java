package com.example.BuildingManagement.invitation.Model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvitationRequest {

    @Email(message = "Please provide a valid email address")
    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Room ID is required")
    private Long roomId;
}
