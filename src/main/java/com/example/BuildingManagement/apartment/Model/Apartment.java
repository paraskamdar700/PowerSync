package com.example.BuildingManagement.apartment.Model;

import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.user.Model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "apartments")
@Getter
@Setter
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @JsonIgnore
    private User landlord;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("apartment")
    @JsonIgnore
    private List<Room> rooms;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}

