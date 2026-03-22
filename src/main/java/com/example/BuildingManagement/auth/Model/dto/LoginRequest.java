package com.example.BuildingManagement.auth.Model.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String passwordHash;
}
