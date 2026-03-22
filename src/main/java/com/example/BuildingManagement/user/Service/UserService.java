package com.example.BuildingManagement.user.Service;


import com.example.BuildingManagement.user.Model.User;

import java.util.*;

public interface UserService {

    User addUser(User user);

    void deleteUser(Long id);

    User getUserById(Long id);

    List<User> getAllUsers();

}
