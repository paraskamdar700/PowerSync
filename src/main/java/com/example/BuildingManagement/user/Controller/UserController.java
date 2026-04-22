package com.example.BuildingManagement.user.Controller;

import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        // Returns the actual User entity from the DB based on the JWT token
        return ResponseEntity.ok(principal.getUser());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the " + user.getRole() + " dashboard, " + user.getFullname() + "!");
        response.put("role", user.getRole().name());
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        
        // You can easily inject Repositories here later to fetch stats specific to LANDLORD vs TENANT
        // Example: if (user.getRole() == UserRole.LANDLORD) { response.put("totalMonies", billRepo.sumAll()) }

        return ResponseEntity.ok(response);
    }
}

