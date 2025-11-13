package com.swp.myleague.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.payload.request.SignupRequest;
import com.swp.myleague.security.service.UserDetailsImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${myleague.app.jwtSecret}")
  private String jwtSecret;

  @Value("${myleague.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${myleague.app.jwtCookieName}")
  private String jwtCookie;

  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, jwtCookie);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
    String jwt = generateToken(userPrincipal);
    ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt).path("/").maxAge(jwtExpirationMs).httpOnly(true)
        .build();
    return cookie;
  }

  public ResponseCookie getCleanJwtCookie() {
    ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/").build();
    return cookie;
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }

  // public String generateTokenFromUsername(String username) {
  // return Jwts.builder()
  // .setSubject(username)
  // .setIssuedAt(new Date())
  // .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
  // .signWith(key(), SignatureAlgorithm.HS256)
  // .compact();
  // }

  public String generateToken(UserDetailsImpl user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .claim("role", user.getAuthorities().iterator().next().getAuthority()) // eg. "ROLE_ADMIN"
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateVerificationToken(SignupRequest request) {
    return Jwts.builder()
        .claim("username", request.getUsername())
        .claim("email", request.getEmail())
        .claim("password", request.getPassword())
        .claim("role", request.getRole())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 phút
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public SignupRequest parseVerificationToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key())
          .build()
          .parseClaimsJws(token)
          .getBody();
      SignupRequest signupRequest = new SignupRequest();
      signupRequest.setUsername(claims.get("username", String.class));
      signupRequest.setPassword(claims.get("password", String.class));
      signupRequest.setEmail(claims.get("email", String.class));
      signupRequest.setRole("user");
      return signupRequest;
    } catch (JwtException e) {
      return null;
    }
  }

  public String generateResetPasswordToken(User user) {
    return Jwts.builder()
        .setSubject(user.getUserId().toString()) // UUID dưới dạng string
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 phút
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public UUID parseResetPasswordToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(key())
          .build()
          .parseClaimsJws(token)
          .getBody();
      return UUID.fromString(claims.getSubject()); // Convert lại UUID
    } catch (Exception e) {
      return null;
    }
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key()) // ✅ Dùng đúng key như mọi chỗ khác
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

}
