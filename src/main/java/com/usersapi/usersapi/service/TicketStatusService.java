package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.TicketStatus;
import com.usersapi.usersapi.repository.TicketStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketStatusService {

    private final TicketStatusRepository ticketStatusRepository;

    public TicketStatusService(TicketStatusRepository ticketStatusRepository) {
        this.ticketStatusRepository = ticketStatusRepository;
    }

    public Optional<TicketStatus> findById(Integer id) {
        return ticketStatusRepository.findById(id);
    }

    public Optional<TicketStatus> findByName(String name) {
        return ticketStatusRepository.findByNameIgnoreCase(name);
    }

    public List<TicketStatus> findAll() {
        return ticketStatusRepository.findAll();
    }
}
