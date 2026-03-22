package com.example.BuildingManagement.invitation.Controller;

import com.example.BuildingManagement.invitation.Model.InvitationRequest;
import com.example.BuildingManagement.invitation.Service.InvitationService;
import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<String> inviteTenant(@RequestBody InvitationRequest request,
                                               @AuthenticationPrincipal UserPrincipal landlordPrincipal) {

        User landlord = landlordPrincipal.getUser();

        invitationService.sendInvitation(request.getEmail(), request.getRoomId(), landlord);

        return ResponseEntity.ok("Invitation sent successfully to " + request.getEmail());
    }
}
