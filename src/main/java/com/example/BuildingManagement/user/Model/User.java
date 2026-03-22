package com.example.BuildingManagement.user.Model;


import com.example.BuildingManagement.common.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255) // Database NOT NULL constraint
    @NotBlank(message = "Fullname is required")
    private String fullname;

    @Column(nullable = false, unique = true) // Database UNIQUE constraint
    @Email(message = "Invalid email format")
    private String email;

    @Column(name = "contact_no", unique = true, length = 10)
    private String contactNo;

    @Column(name = "password_hash") // Nullable for OAuth2 users
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'LANDLORD'", nullable = false)
    private UserRole role = UserRole.LANDLORD;

    @ManyToOne
    @JoinColumn(name = "landlord_id") // This creates the FK in the database
    private User landlord;

    @Column(columnDefinition = "varchar(20)") // Default value constraint
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}