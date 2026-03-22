package com.example.BuildingManagement.room.Model;

import com.example.BuildingManagement.apartment.Model.Apartment;
import com.example.BuildingManagement.user.Model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomNumber;

    private Integer floorNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties("rooms")
    @JsonIgnore
    private Apartment apartment;

    // Initially NULL as per your Phase 2 requirement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_tenant_id")
    private User currentTenant;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}