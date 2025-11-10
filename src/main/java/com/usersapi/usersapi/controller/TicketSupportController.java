package com.usersapi.usersapi.controller;

import com.usersapi.usersapi.dto.TicketAnswerRequest;
import com.usersapi.usersapi.dto.TicketCreateRequest;
import com.usersapi.usersapi.dto.TicketResponse;
import com.usersapi.usersapi.model.TicketSupport;
import com.usersapi.usersapi.service.TicketSupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
public class TicketSupportController {

    private final TicketSupportService ticketSupportService;

    public TicketSupportController(TicketSupportService ticketSupportService) {
        this.ticketSupportService = ticketSupportService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody TicketCreateRequest request) {
        if (request.getIdplanifikauser() == null || request.getTitle() == null || request.getDescription() == null) {
            return ResponseEntity.badRequest().build();
        }

        TicketSupport ticket = ticketSupportService.create(
                request.getIdplanifikauser(),
                request.getTitle(),
                request.getDescription(),
                request.getIddrimsoftuser(),
                request.getIdticketstatus()
        );

        return ResponseEntity.ok(toResponse(ticket));
    }

    @GetMapping
    public List<TicketResponse> list(
            @RequestParam(required = false) Integer idplanifikauser,
            @RequestParam(required = false) Integer iddrimsoftuser
    ) {
        List<TicketSupport> list;
        if (idplanifikauser != null) {
            list = ticketSupportService.findByPlanifikaUser(idplanifikauser);
        } else if (iddrimsoftuser != null) {
            list = ticketSupportService.findByDrimsoftUser(iddrimsoftuser);
        } else {
            list = ticketSupportService.findAll();
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Integer id) {
        return ticketSupportService.findById(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/answer")
    public ResponseEntity<TicketResponse> addAnswer(
            @PathVariable Integer id,
            @RequestBody TicketAnswerRequest request) {
        try {
            if (request.getAnswer() == null || request.getAnswer().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            TicketSupport ticket = ticketSupportService.updateAnswer(
                    id,
                    request.getAnswer(),
                    request.getIddrimsoftuser()
            );

            return ResponseEntity.ok(toResponse(ticket));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status/{statusId}")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Integer id,
            @PathVariable Integer statusId) {
        try {
            System.out.println("DEBUG Controller: Recibido request para actualizar ticket " + id + " a estado " + statusId);
            TicketSupport ticket = ticketSupportService.updateStatus(id, statusId);
            return ResponseEntity.ok(toResponse(ticket));
        } catch (RuntimeException e) {
            System.err.println("ERROR Controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/assign/{userId}")
    public ResponseEntity<TicketResponse> assignUser(
            @PathVariable Integer id,
            @PathVariable Integer userId) {
        try {
            TicketSupport ticket = ticketSupportService.assignUser(id, userId);
            return ResponseEntity.ok(toResponse(ticket));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<TicketResponse> markAsRead(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Integer> body) {
        try {
            Integer userId = body != null ? body.get("iddrimsoftuser") : null;
            TicketSupport ticket = ticketSupportService.markAsRead(id, userId);
            return ResponseEntity.ok(toResponse(ticket));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private TicketResponse toResponse(TicketSupport t) {
        TicketResponse r = new TicketResponse();
        r.setIdtickets(t.getIdTickets());
        r.setIdplanifikauser(t.getIdPlanifikaUser());
        if (t.getStatus() != null) {
            r.setIdticketstatus(t.getStatus().getIdTicketStatus());
            r.setTicketstatusname(t.getStatus().getName());
        }
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setAnswer(t.getAnswer());
        if (t.getDrimsoftUser() != null) {
            r.setIddrimsoftuser(t.getDrimsoftUser().getIdUser());
            r.setDrimsoftusername(t.getDrimsoftUser().getName());
        }
        return r;
    }
}
