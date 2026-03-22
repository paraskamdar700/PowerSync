



package com.example.BuildingManagement.invitation.Service;


import com.example.BuildingManagement.auth.Model.dto.TenantRegistrationRequest;
import com.example.BuildingManagement.common.enums.InvitationStatus;
import com.example.BuildingManagement.common.enums.UserRole;
import com.example.BuildingManagement.invitation.Model.Invitation;
import com.example.BuildingManagement.invitation.Repository.InvitationRepo;
import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.room.Repository.RoomRepo;
import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvitationServiceIMPL implements InvitationService{


    private final InvitationRepo invitationRepo;
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender; // You'll need to add this starter

    @Autowired
    public InvitationServiceIMPL(InvitationRepo invitationRepo, UserRepo userRepo, RoomRepo roomRepo, BCryptPasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.invitationRepo =  invitationRepo;
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvitation(String email, Long roomId, User landlord) {
        Room room = roomRepo.findByRoomNumber(roomId).orElseThrow();
        String code = UUID.randomUUID().toString();
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setInviteCode(code);
        invitation.setRoom(room);
        invitation.setLandlord(landlord);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));
        invitationRepo.save(invitation);
        // Logic to send email (See Step 4)
        sendEmail(email, code);

    }


    @Override
    public Invitation validateInvite(String code) {
        Invitation invite = invitationRepo.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid Invitation Link"));

        if (invite.isExpired()) {
            invite.setStatus(InvitationStatus.EXPIRED);
            invitationRepo.save(invite);
            throw new RuntimeException("This invitation has expired.");
        }

        if (invite.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This invitation has already been used.");
        }

        return invite;
    }
    @Override
    @Transactional
    public void completeOnboarding(TenantRegistrationRequest request) {
        // Re-validate invite
        Invitation invite = validateInvite(request.getInviteCode());

        // Ensure email matches the one invited
        if (!invite.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new RuntimeException("Registration email must match the invited email.");
        }

        // Create Tenant User
        User tenant = new User();
        tenant.setFullname(request.getFullname());
        tenant.setEmail(request.getEmail());
        tenant.setContactNo(request.getContactNo());
        tenant.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        tenant.setRole(UserRole.TENANT);
        tenant.setLandlord(invite.getLandlord()); // Link to Landlord
        userRepo.save(tenant);

        // Link Tenant to Room
        Room room = invite.getRoom();
        room.setCurrentTenant(tenant);
        roomRepo.save(room);

        // Mark Invite as Accepted
        invite.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invite);
    }

    @Override
    @Transactional
    public User completeOAuthOnboarding(String email, String fullname, String inviteCode) {
        // 1. Reuse your existing validation logic
        Invitation invite = validateInvite(inviteCode);

        if (!invite.getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Google account email does not match the invited email.");
        }

        // 2. Create User
        User tenant = new User();
        tenant.setFullname(fullname);
        tenant.setEmail(email);
        tenant.setRole(UserRole.TENANT);
        tenant.setProvider("google"); // Track that this is an OAuth user
        tenant.setLandlord(invite.getLandlord());
        userRepo.save(tenant);

        // 3. Link to Room
        Room room = invite.getRoom();
        room.setCurrentTenant(tenant);
        roomRepo.save(room);

        invite.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invite);

        return tenant;
    }

    @Override
    @Async
    public void sendEmail(String toEmail, String inviteCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Invitation to join PowerSync");

        String registrationUrl = "http://localhost:3000/register?code=" + inviteCode;

        message.setText("Welcome to PowerSync!\n\n" +
                "Your landlord has invited you to join their property. " +
                "Please click the link below to register and accept your room assignment:\n" +
                registrationUrl + "\n\n" +
                "If you already have an account, please log in and enter the code: " + inviteCode);

        mailSender.send(message);
    }
}
