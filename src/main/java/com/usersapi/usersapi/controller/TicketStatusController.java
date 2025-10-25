package com.usersapi.usersapi.controller;

import com.usersapi.usersapi.model.TicketStatus;
import com.usersapi.usersapi.service.TicketStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket-status")
public class TicketStatusController {

    private final TicketStatusService ticketStatusService;

    public TicketStatusController(TicketStatusService ticketStatusService) {
        this.ticketStatusService = ticketStatusService;
    }

    @GetMapping
    public List<TicketStatus> getAllStatuses() {
        return ticketStatusService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketStatus> getStatusById(@PathVariable Integer id) {
        return ticketStatusService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
