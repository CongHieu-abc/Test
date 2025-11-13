package com.swp.myleague.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.swp.myleague.security.jwt.AuthEntryPointJwt;
import com.swp.myleague.security.jwt.AuthTokenFilter;
import com.swp.myleague.security.oauth2.CustomOAuth2UserService;
import com.swp.myleague.security.oauth2.OAuth2SuccessHandler;
import com.swp.myleague.security.service.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;

  private final PasswordEncoder passwordEncoder;
  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Autowired
  private OAuth2SuccessHandler oAuth2LoginSuccessHandler;

  WebSecurityConfig(PasswordEncoder passwordEncoder, CustomOAuth2UserService customOAuth2UserService) {
    this.passwordEncoder = passwordEncoder;
    this.customOAuth2UserService = customOAuth2UserService;
  }

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/home", "/auth/**", "/login/**", "/oauth2/**", "/product/**", "/cart/**", "/error", "/feedback/**", "/ws/**", "/news/**", "/match/**", "/tables/**", "/club/**", "/stastics/**", "/WebSocket2/**").permitAll()
            .requestMatchers("/css/**", "/images/**", "/component/**", "/fonts/**").permitAll()
            .requestMatchers("/clubmanager/**").hasRole("CLUB_MANAGER")
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService))
            .successHandler(oAuth2LoginSuccessHandler));

    http.authenticationProvider(authenticationProvider(passwordEncoder));

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
