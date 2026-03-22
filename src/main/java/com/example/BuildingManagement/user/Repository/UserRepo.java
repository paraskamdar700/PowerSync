package com.example.BuildingManagement.user.Repository;

import com.example.BuildingManagement.user.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

Optional<User> findByEmail(String email);

}
