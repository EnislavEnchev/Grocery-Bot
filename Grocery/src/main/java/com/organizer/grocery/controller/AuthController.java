package com.organizer.grocery.controller;

import com.organizer.grocery.aws.SnsEmailService;
import com.organizer.grocery.config.JwtUtils;
import com.organizer.grocery.dto.AuthResponse;
import com.organizer.grocery.dto.LoginRequest;
import com.organizer.grocery.dto.SignupRequest;
import com.organizer.grocery.model.Role;
import com.organizer.grocery.model.User;
import com.organizer.grocery.repository.RoleRepository;
import com.organizer.grocery.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final SnsEmailService snsEmailService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils, RoleRepository roleRepository, SnsEmailService snsEmailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
        this.snsEmailService = snsEmailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Invalid email or password!");
        }
        String token = jwtUtils.generateToken(authentication);
        AuthResponse authResponse = new AuthResponse(
                token,
                userOptional.get().getFullName(),
                userOptional.get().getEmail()
        );
        System.out.println(userOptional.get().getAuthorities());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Emial is already taken!");
        }

        User user = new User();
        user.setUsername(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setAddress(signupRequest.getAddress());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setFullName(signupRequest.getFullName());
        Optional<Role> userRole = roleRepository.findByName("USER");
        if(userRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: User role not found!");
        }
        user.setRoles(Set.of(userRole.get()));
        System.out.println(user.getAuthorities());
        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), signupRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateToken(authentication);
        try {
            snsEmailService.subscribeEmail(user.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Unable to subscribe email for notifications.");
        }
        AuthResponse authResponse = new AuthResponse(
                token,
                user.getFullName(),
                user.getEmail()
        );
        return ResponseEntity.ok(authResponse);
    }
}