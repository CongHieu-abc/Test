package com.swp.myleague.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.repo.UserRepo;
import com.swp.myleague.security.jwt.JwtUtils;
import com.swp.myleague.security.service.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepo userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        // Tìm user trong database
        User user = userRepository.findByEmail(email).orElseThrow();

        // Chuyển User -> UserDetailsImpl
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Tạo JWT cookie
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // Gửi cookie về client
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        // Redirect về frontend
        response.sendRedirect("http://localhost:8080/home");
    }
}
