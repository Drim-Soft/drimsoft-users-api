package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.TicketStatus;
import com.usersapi.usersapi.model.TicketSupport;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.repository.TicketSupportRepository;
import com.usersapi.usersapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketSupportService {

    private final TicketSupportRepository ticketSupportRepository;
    private final TicketStatusService ticketStatusService;
    private final UserRepository userRepository;

    public TicketSupportService(TicketSupportRepository ticketSupportRepository,
                                TicketStatusService ticketStatusService,
                                UserRepository userRepository) {
        this.ticketSupportRepository = ticketSupportRepository;
        this.ticketStatusService = ticketStatusService;
        this.userRepository = userRepository;
    }

    public TicketSupport create(Integer idPlanifikaUser,
                                String title,
                                String description,
                                Integer idDrimsoftUser,
                                Integer idTicketStatus) {
        TicketSupport ticket = new TicketSupport();
        ticket.setIdPlanifikaUser(idPlanifikaUser);
        ticket.setTitle(title);
        ticket.setDescription(description);

        if (idDrimsoftUser != null) {
            Optional<UserDrimsoft> userOpt = userRepository.findById(idDrimsoftUser);
            userOpt.ifPresent(ticket::setDrimsoftUser);
        }

        TicketStatus status = null;
        if (idTicketStatus != null) {
            status = ticketStatusService.findById(idTicketStatus).orElse(null);
        }
        if (status == null) {
            // Default to PENDING (ID: 1)
            status = ticketStatusService.findById(1).orElse(null);
        }
        ticket.setStatus(status);

        return ticketSupportRepository.save(ticket);
    }

    public Optional<TicketSupport> findById(Integer id) {
        return ticketSupportRepository.findById(id);
    }

    public List<TicketSupport> findAll() {
        return ticketSupportRepository.findAll();
    }

    public List<TicketSupport> findByPlanifikaUser(Integer idPlanifikaUser) {
        return ticketSupportRepository.findByIdPlanifikaUser(idPlanifikaUser);
    }

    public List<TicketSupport> findByDrimsoftUser(Integer idUser) {
        return ticketSupportRepository.findByDrimsoftUser_IdUser(idUser);
    }

    public TicketSupport updateAnswer(Integer ticketId, String answer, Integer idDrimsoftUser) {
        TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setAnswer(answer);

        // Asignar usuario si se proporciona
        if (idDrimsoftUser != null) {
            Optional<UserDrimsoft> userOpt = userRepository.findById(idDrimsoftUser);
            userOpt.ifPresent(ticket::setDrimsoftUser);
        }

        // Cambiar estado a ANSWERED (ID: 3)
        TicketStatus answeredStatus = ticketStatusService.findById(3).orElse(null);

        if (answeredStatus != null) {
            ticket.setStatus(answeredStatus);
        }

        return ticketSupportRepository.save(ticket);
    }

    public TicketSupport updateStatus(Integer ticketId, Integer statusId) {
        System.out.println("DEBUG: Buscando ticket ID: " + ticketId);
        TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + ticketId));

        System.out.println("DEBUG: Ticket encontrado. Buscando estado ID: " + statusId);
        TicketStatus status = ticketStatusService.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + statusId));

        System.out.println("DEBUG: Estado encontrado: " + status.getName());
        ticket.setStatus(status);
        TicketSupport saved = ticketSupportRepository.save(ticket);
        System.out.println("DEBUG: Ticket actualizado exitosamente");
        return saved;
    }

    public TicketSupport assignUser(Integer ticketId, Integer idDrimsoftUser) {
        TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        UserDrimsoft user = userRepository.findById(idDrimsoftUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ticket.setDrimsoftUser(user);

        // Cambiar a estado IN_PROGRESS (ID: 2)
        TicketStatus inProgressStatus = ticketStatusService.findById(2).orElse(ticket.getStatus());

        ticket.setStatus(inProgressStatus);
        return ticketSupportRepository.save(ticket);
    }

    public TicketSupport markAsRead(Integer ticketId, Integer idDrimsoftUser) {
        TicketSupport ticket = ticketSupportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // Asignar usuario si se proporciona
        if (idDrimsoftUser != null) {
            Optional<UserDrimsoft> userOpt = userRepository.findById(idDrimsoftUser);
            userOpt.ifPresent(ticket::setDrimsoftUser);
        }

        // Cambiar a estado IN_PROGRESS (ID: 2)
        TicketStatus readStatus = ticketStatusService.findById(2).orElse(ticket.getStatus());

        ticket.setStatus(readStatus);
        return ticketSupportRepository.save(ticket);
    }
}
