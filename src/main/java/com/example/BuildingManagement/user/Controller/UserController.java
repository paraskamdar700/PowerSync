package com.example.BuildingManagement.user.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/profile")
    public String getProfile() {
        return "This is protected profile data";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Protected dashboard";
    }
}

