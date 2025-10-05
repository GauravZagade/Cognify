package com.cognify.service;

import com.cognify.dto.RegisterRequest;
import com.cognify.dto.UserDto;
import com.cognify.entity.User;
import com.cognify.entity.UserRole;
import com.cognify.exception.ResourceNotFoundException;
import com.cognify.repository.UserRepository;
import com.cognify.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    
    // Spring Security UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
    
    @Override
    @Transactional
    public UserDto createUser(RegisterRequest request) {
        log.info("Creating a new user with username: {}", request.getUsername());
        
        // Validate username doesn't exist
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }
        
        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }
        
        // Map RegisterRequest to User entity
        User user = modelMapper.map(request, User.class);
        
        // Encode password (CRITICAL for security)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set role
        try {
            user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", request.getRole());
            throw new RuntimeException("Invalid role: " + request.getRole());
        }
        
        // Map profile information if provided
        if (request.getProfile() != null) {
            user.setFirstName(request.getProfile().getFirstName());
            user.setLastName(request.getProfile().getLastName());
            user.setSchoolName(request.getProfile().getSchoolName());
            user.setPhone(request.getProfile().getPhone());
        }
        
        // Set active status
        user.setIsActive(true);
        
        // Save user
        user = userRepository.save(user);
        log.info("Created user with ID: {}", user.getId());
        
        // Return UserDto (without password)
        return modelMapper.map(user, UserDto.class);
    }
    
    @Override
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return modelMapper.map(user, UserDto.class);
    }
    
    @Override
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Map updated fields (excluding password and sensitive fields)
        modelMapper.map(userDto, user);
        user.setId(id); // ensure id remains same
        
        user = userRepository.save(user);
        log.info("Updated user with ID: {}", id);
        
        return modelMapper.map(user, UserDto.class);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
        log.info("Deleted user with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setIsActive(true);
        userRepository.save(user);
        log.info("Activated user with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Deactivated user with ID: {}", id);
    }
    
    @Override
    public User findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}