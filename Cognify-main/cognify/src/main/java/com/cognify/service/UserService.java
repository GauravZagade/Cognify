package com.cognify.service;

import com.cognify.dto.RegisterRequest;
import com.cognify.dto.UserDto;
import com.cognify.entity.User;

import java.util.List;

public interface UserService {
    
    UserDto createUser(RegisterRequest request);
    
    UserDto getUserById(Long id);
    
    List<UserDto> getAllUsers();
    
    UserDto updateUser(Long id, UserDto userDto);
    
    void deleteUser(Long id);
    
    void activateUser(Long id);
    
    void deactivateUser(Long id);
    
    User findByUsername(String username);
}