package com.cognify.controller;

import com.cognify.dto.ApiResponse;
import com.cognify.dto.LoginRequest;
import com.cognify.dto.RegisterRequest;
import com.cognify.dto.UserDto;
import com.cognify.entity.User;
import com.cognify.service.UserService;
import com.cognify.service.UserServiceImpl;
import com.cognify.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registration attempt for username: {}", request.getUsername());
            
            // Create user and get UserDto
            UserDto userDto = userService.createUser(request);
            
            log.info("User registered successfully with ID: {}", userDto.getId());
            
            return new ResponseEntity<>(
                new ApiResponse(true, "User registered successfully", userDto),
                HttpStatus.CREATED
            );
                    
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            return new ResponseEntity<>(
                new ApiResponse(false, e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for username: {}", request.getUsername());
            
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(), 
                    request.getPassword()
                )
            );
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            User user = userService.findByUsername(request.getUsername());
            
            // Generate tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("expires_in", 3600);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole().name().toLowerCase());
            response.put("user", userInfo);
            
            log.info("Login successful for username: {}", request.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            log.error("Login failed - Invalid credentials for username: {}", request.getUsername());
            return new ResponseEntity<>(
                new ApiResponse(false, "Invalid username or password"),
                HttpStatus.UNAUTHORIZED
            );
        }
    }
}