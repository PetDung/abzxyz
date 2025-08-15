package com.petd.tiktok_system_be.securityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petd.tiktok_system_be.constant.SecurityConstants;
import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.util.JwtUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomPreFilter extends OncePerRequestFilter {

  JwtUtils jwtUtils;
  CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    AntPathMatcher pathMatcher  = new AntPathMatcher();
    for (String pattern : SecurityConstants.PUBLIC_URLS) {
      if (pathMatcher.match(pattern, request.getRequestURI())) {
        filterChain.doFilter(request, response);
        return;
      }
    }

    String token = jwtUtils.getTokenByRequestHeader(request);
    log.info("token: {}", StringUtils.isBlank(token));

    if(StringUtils.isBlank(token) || !jwtUtils.validateToken(token)) {
      log.info("Invalid JWT token");
      unauthorized(response, "Token isn't valid", 40001);
      return;
    }

    try {
      String email  = jwtUtils.getUsernameFromToken(token);
      if(StringUtils.isNotEmpty(email) || SecurityContextHolder.getContext().getAuthentication() == null){

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        SecurityContext contextHolder = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        contextHolder.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(contextHolder);

      }
    }catch (Exception e){
      log.error(e.getMessage());
      unauthorized(response, "Token isn't valid", 40001);
      return;
    }
    filterChain.doFilter(request, response);
  }

  public static void unauthorized(HttpServletResponse response, String message, int code) throws IOException {
    ApiResponse apiResponse = ApiResponse.builder()
        .message(message)
        .code(code)
        .build();

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(apiResponse));
  }

}
