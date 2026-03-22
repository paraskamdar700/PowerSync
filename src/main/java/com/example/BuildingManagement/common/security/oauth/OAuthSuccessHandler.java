package com.example.BuildingManagement.common.security.oauth;

import com.example.BuildingManagement.common.enums.UserRole;
import com.example.BuildingManagement.common.security.jwt.JwtUtils;
import com.example.BuildingManagement.invitation.Service.InvitationService;
import com.example.BuildingManagement.user.Model.User;
import com.example.BuildingManagement.user.Repository.UserRepo;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final JwtUtils jwtUtils;
    private final InvitationService  invitationService;

    @Autowired
    public OAuthSuccessHandler(UserRepo userRepo, JwtUtils jwtUtils,@Lazy InvitationService invitationService) {
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.invitationService = invitationService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String providerId = oauthUser.getAttribute("sub");
        String provider = authToken.getAuthorizedClientRegistrationId();

        if (email == null) {
            throw new ServletException("Email not provided by OAuth provider");
        }

        String inviteCode = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("pending_invite_code".equals(cookie.getName())) {
                    inviteCode = cookie.getValue();
                    break;
                }
            }
        }

        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null && inviteCode != null) {
            // This is a new Tenant joining via OAuth2
            user = invitationService.completeOAuthOnboarding(email, name , inviteCode);
        } else if (user == null) {
            // This is a standard Landlord/User registering via OAuth2
            user = new User();
            user.setEmail(email);
            user.setFullname(name);
            user.setProviderId(providerId);
            user.setProvider(provider);
            user.setRole(UserRole.LANDLORD); // Default for new Google signups
            user.setLandlord(null);
             userRepo.save(user);
        }



            UserPrincipal userPrincipal = new UserPrincipal(Optional.of(user));



        // Wrap your entity in UserPrincipal so JwtUtils can read authorities

        // Generate Token using UserDetails to include "roles" in claims
        String token = jwtUtils.generateToken(userPrincipal);

        // Set HttpOnly Cookie for security
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Should be true in production (HTTPS)
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        Cookie clearInvite = new Cookie("pending_invite_code", null);
        clearInvite.setMaxAge(0);
        clearInvite.setPath("/");
        response.addCookie(clearInvite);

        // Redirect based on role
        String targetUrl = user.getRole() == UserRole.LANDLORD ? "/admin/dashboard" : "/tenant/dashboard";
        response.sendRedirect("http://localhost:5173" + targetUrl);
    }
}