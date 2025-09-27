package com.usersapi.usersapi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.usersapi.usersapi.model.Authentication;

import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, Integer> {

    // Used for login
    Optional<Authentication> findByEmailAndPassword(String email, String password);
}
