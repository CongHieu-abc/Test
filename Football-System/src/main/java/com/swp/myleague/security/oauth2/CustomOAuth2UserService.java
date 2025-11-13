package com.swp.myleague.security.oauth2;

import java.util.Collections;
import java.util.Optional;

import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.swp.myleague.model.entities.Role;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.repo.UserRepo;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private UserRepo userRepository;

    private PasswordEncoder encoder;

    public CustomOAuth2UserService(PasswordEncoder encoder, UserRepo userRepo) {
        this.encoder = encoder;
        this.userRepository = userRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        // Check nếu user chưa tồn tại thì tạo mới
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = new User();
        if (userOptional.isEmpty()) {
            user.setUsername(email.split("@")[0]);
            user.setEmail(email);
            user.setPassword(encoder.encode(UUID.randomUUID().toString())); // hoặc một chuỗi ngẫu nhiên
            user.setRole(Role.USER);
            userRepository.save(user);
        } else {
            user = userOptional.orElseThrow();
        }
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                "email" // thuộc tính dùng làm "username"
        );
    }
}
