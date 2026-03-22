package com.example.BuildingManagement.invitation.Repository;

import com.example.BuildingManagement.common.enums.InvitationStatus;
import com.example.BuildingManagement.invitation.Model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface InvitationRepo extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInviteCode(String inviteCode);
    Optional<Invitation> findByEmailAndStatus(String email, InvitationStatus status);
}
