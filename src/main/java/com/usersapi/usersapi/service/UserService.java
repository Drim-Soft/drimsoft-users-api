package com.usersapi.usersapi.service;

import org.springframework.stereotype.Service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.model.UserStatus;
import com.usersapi.usersapi.repository.RoleRepository;
import com.usersapi.usersapi.repository.UserRepository;
import com.usersapi.usersapi.repository.UserStatusRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserStatusRepository userStatusRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userStatusRepository = userStatusRepository;
    }

    public List<UserDrimsoft> findAll() { return userRepository.findAll(); }

    public Optional<UserDrimsoft> findById(Integer id) { return userRepository.findById(id); }

    public UserDrimsoft save(UserDrimsoft user) { return userRepository.save(user); }

    public void delete(Integer id) { userRepository.deleteById(id); }

    public UserDrimsoft updateRole(Integer userId, Integer roleId) {
        UserDrimsoft user = userRepository.findById(userId).orElseThrow();
        Role role = roleRepository.findById(roleId).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    public UserDrimsoft updateStatus(Integer userId, Integer statusId) {
        UserDrimsoft user = userRepository.findById(userId).orElseThrow();
        UserStatus status = userStatusRepository.findById(statusId).orElseThrow();
        user.setStatus(status);
        return userRepository.save(user);
    }
}
