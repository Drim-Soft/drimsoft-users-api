package com.usersapi.usersapi.repository;

import com.usersapi.usersapi.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatus, Integer> {
    Optional<TicketStatus> findByNameIgnoreCase(String name);
}
