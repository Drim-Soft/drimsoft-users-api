package com.usersapi.usersapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.usersapi.usersapi.model.UserDrimsoft;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserDrimsoft, Integer> {
    UserDrimsoft findBySupabaseUserId(UUID supabaseUserId);
}
