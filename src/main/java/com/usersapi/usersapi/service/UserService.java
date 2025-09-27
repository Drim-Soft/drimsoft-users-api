package com.usersapi.usersapi.service;

import org.springframework.stereotype.Service;

import com.usersapi.usersapi.model.Role;
import com.usersapi.usersapi.model.User;
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

    public List<User> findAll() { return userRepository.findAll(); }

    public Optional<User> findById(Integer id) { return userRepository.findById(id); }

    public User save(User user) { return userRepository.save(user); }

    public void delete(Integer id) { userRepository.deleteById(id); }

    public User updateRole(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId).orElseThrow();
        Role role = roleRepository.findById(roleId).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    public User updateStatus(Integer userId, Integer statusId) {
        User user = userRepository.findById(userId).orElseThrow();
        UserStatus status = userStatusRepository.findById(statusId).orElseThrow();
        user.setStatus(status);
        return userRepository.save(user);
    }
}
