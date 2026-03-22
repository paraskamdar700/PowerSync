package com.example.BuildingManagement.auth.Model.dto;

import lombok.Data;

@Data
public class TenantRegistrationRequest {
    private String fullname;
    private String email;
    private String password;
    private String contactNo;
    private String inviteCode; // Used to link back to the invitation record
}