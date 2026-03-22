package com.example.BuildingManagement.auth.Controller;

import com.example.BuildingManagement.auth.Model.dto.LoginRequest;
import com.example.BuildingManagement.auth.Model.dto.TenantRegistrationRequest;
import com.example.BuildingManagement.common.security.jwt.JwtUtils;
import com.example.BuildingManagement.invitation.Model.Invitation;
import com.example.BuildingManagement.invitation.Service.InvitationService;
import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Service.UserServiceIMPL;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;



@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private JwtUtils jwtUtils;
    private AuthenticationManager authenticationManager;
    private UserServiceIMPL  userServiceIMPL;
    private InvitationService  invitationService;
    
    @Autowired
    public void setMyUserDetailService(AuthenticationManager authenticationManager, UserServiceIMPL userServiceIMPL, InvitationService invitationService, JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userServiceIMPL = userServiceIMPL;
        this.invitationService= invitationService;
    }

    @GetMapping("/")
    public String index() {
        return "index welcome";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPasswordHash())
        );

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Generate token passing the whole userDetails object to include roles in claims
            String token = jwtUtils.generateToken(userDetails);

            // 4. Set secure Cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Ensure your local env uses HTTPS or change for dev
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user)
    {
        return userServiceIMPL.addUser(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyInvite(@RequestParam String code) {
        Invitation invite = invitationService.validateInvite(code);
        // Return only what the frontend needs to pre-fill the form
        return ResponseEntity.ok(Map.of(
                "email", invite.getEmail(),
                "landlordName", invite.getLandlord().getFullname(),
                "roomNumber", invite.getRoom().getRoomNumber()
        ));
    }

    // Final submission from the registration form
    @PostMapping("/register-tenant")
    public ResponseEntity<String> registerTenant(@RequestBody TenantRegistrationRequest request) {
        invitationService.completeOnboarding(request);
        return ResponseEntity.ok("Registration successful! You are now linked to your room.");
    }



}
