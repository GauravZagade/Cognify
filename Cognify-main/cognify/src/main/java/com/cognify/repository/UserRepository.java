package com.cognify.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognify.entity.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);


}
