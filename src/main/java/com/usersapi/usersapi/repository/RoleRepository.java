package com.usersapi.usersapi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.usersapi.usersapi.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

}