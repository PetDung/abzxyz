package com.petd.tiktok_system_be.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String secret;

  @Getter
  @Value("${jwt.expiration}")
  private long expiration;

  private Key jwtKey;

  @PostConstruct
  public void init() {
    System.out.println("Loaded secret: " + secret);
    byte[] keyBytes = Base64.getDecoder().decode(secret);
    this.jwtKey = Keys.hmacShaKeyFor(keyBytes);
  }
  public String generateToken(String username) throws Exception {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(jwtKey)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    validateToken(token);
    return Jwts.parserBuilder().setSigningKey(jwtKey).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(jwtKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
      return true;
    } catch (Exception ex) {
      log.error(ex.getMessage());
      return false;
    }
  }

  public String getTokenByRequest(HttpServletRequest request){
    String token = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }
    return token;
  }

  public String getTokenByRequestHeader(HttpServletRequest request) {
      return request.getHeader("Authorization");
  }

}
