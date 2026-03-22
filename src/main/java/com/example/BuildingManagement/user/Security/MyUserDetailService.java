package com.example.BuildingManagement.user.Security;

import com.example.BuildingManagement.user.Repository.UserRepo;
import com.example.BuildingManagement.user.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService   implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

       Optional<User> user =  userRepo.findByEmail(email);

       if(user.isEmpty()){
            throw new UsernameNotFoundException(email);
        }
        return new UserPrincipal(user);

    }
}
