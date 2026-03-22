package com.example.BuildingManagement.invitation.Service;

import com.example.BuildingManagement.auth.Model.dto.TenantRegistrationRequest;
import com.example.BuildingManagement.invitation.Model.Invitation;
import com.example.BuildingManagement.user.Model.User;

public interface InvitationService {

    void sendInvitation(String email, Long roomId, User landlord);

    void sendEmail(String toEmail, String inviteCode);

    Invitation validateInvite(String code);

    void completeOnboarding(TenantRegistrationRequest request);

    User completeOAuthOnboarding(String email, String fullname, String inviteCode);

    }

