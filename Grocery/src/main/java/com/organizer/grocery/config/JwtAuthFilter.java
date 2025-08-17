package com.organizer.grocery.config;

import com.organizer.grocery.model.User;
import com.organizer.grocery.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    public JwtAuthFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,

            @NonNull
            HttpServletResponse response,

            @NonNull
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateToken(token)) {
                User user = userRepository.findByEmail(jwtUtils.getUsernameFromToken(token))
                        .orElseThrow(() -> new RuntimeException("User not found : " + jwtUtils.getUsernameFromToken(token)));
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());
                System.out.println("Authenticated user: " + user.getEmail() + " with roles: " + user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                System.out.println("Invalid JWT token: " + token);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/auth/login") || path.equals("/auth/signup");
    }
}
