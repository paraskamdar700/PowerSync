package com.example.BuildingManagement.common.security.oauth;


import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Repository.UserRepo;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepo userRepo;

    public CustomOidcUserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {


        // 1️⃣ Let Spring fetch user from Google
        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        String email = oidcUser.getEmail();   // OIDC safe way
        String name = oidcUser.getFullName();
        String providerId = oidcUser.getSubject(); // "sub" field
        System.out.println(email);
        // 2️⃣ Check Database
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isPresent()) {

            User existingUser = userOptional.get();
            if ("local".equals(existingUser.getProvider())) {

                throw new OAuth2AuthenticationException(
                        new OAuth2Error("local_user_conflict", "User already registered with email/password", null)
                );
            }

        }

        // 4️⃣ Return proper OIDC user
        return new DefaultOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}
