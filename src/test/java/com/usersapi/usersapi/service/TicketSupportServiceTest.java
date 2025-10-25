package com.usersapi.usersapi.service;

import com.usersapi.usersapi.model.TicketStatus;
import com.usersapi.usersapi.model.TicketSupport;
import com.usersapi.usersapi.model.UserDrimsoft;
import com.usersapi.usersapi.repository.TicketSupportRepository;
import com.usersapi.usersapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketSupportServiceTest {

    @Mock
    private TicketSupportRepository ticketSupportRepository;
    @Mock
    private TicketStatusService ticketStatusService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketSupportService service;

    @Captor
    ArgumentCaptor<TicketSupport> ticketCaptor;

    private TicketStatus statusPending;
    private TicketStatus statusInProgress;
    private TicketStatus statusAnswered;

    @BeforeEach
    void setUp() {
        statusPending = new TicketStatus();
        statusPending.setIdTicketStatus(1);
        statusPending.setName("PENDING");

        statusInProgress = new TicketStatus();
        statusInProgress.setIdTicketStatus(2);
        statusInProgress.setName("IN_PROGRESS");

        statusAnswered = new TicketStatus();
        statusAnswered.setIdTicketStatus(3);
        statusAnswered.setName("ANSWERED");
    }

    @Test
    void create_defaults_to_PENDING_when_status_null() {
        when(ticketStatusService.findById(1)).thenReturn(Optional.of(statusPending));
        when(ticketSupportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketSupport created = service.create(123, "Titulo", "Desc", null, null);

        assertThat(created.getIdPlanifikaUser()).isEqualTo(123);
        assertThat(created.getTitle()).isEqualTo("Titulo");
        assertThat(created.getDescription()).isEqualTo("Desc");
        assertThat(created.getStatus()).isNotNull();
        assertThat(created.getStatus().getIdTicketStatus()).isEqualTo(1);
    }

    @Test
    void assignUser_sets_user_and_IN_PROGRESS() {
        TicketSupport existing = new TicketSupport();
        existing.setIdTickets(10);
        when(ticketSupportRepository.findById(10)).thenReturn(Optional.of(existing));

        UserDrimsoft user = new UserDrimsoft();
        user.setIdUser(5);
        user.setName("Agent");
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(ticketStatusService.findById(2)).thenReturn(Optional.of(statusInProgress));
        when(ticketSupportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketSupport updated = service.assignUser(10, 5);

        assertThat(updated.getDrimsoftUser()).isNotNull();
        assertThat(updated.getDrimsoftUser().getIdUser()).isEqualTo(5);
        assertThat(updated.getStatus()).isNotNull();
        assertThat(updated.getStatus().getIdTicketStatus()).isEqualTo(2);
    }

    @Test
    void markAsRead_moves_to_IN_PROGRESS_and_optionally_sets_user() {
        TicketSupport existing = new TicketSupport();
        existing.setIdTickets(11);
        when(ticketSupportRepository.findById(11)).thenReturn(Optional.of(existing));
        when(ticketStatusService.findById(2)).thenReturn(Optional.of(statusInProgress));
        when(ticketSupportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketSupport updated = service.markAsRead(11, null);

        assertThat(updated.getStatus()).isNotNull();
        assertThat(updated.getStatus().getIdTicketStatus()).isEqualTo(2);
        assertThat(updated.getDrimsoftUser()).isNull();
    }

    @Test
    void updateAnswer_sets_answer_and_moves_to_ANSWERED() {
        TicketSupport existing = new TicketSupport();
        existing.setIdTickets(12);
        when(ticketSupportRepository.findById(12)).thenReturn(Optional.of(existing));
        when(ticketStatusService.findById(3)).thenReturn(Optional.of(statusAnswered));
        when(ticketSupportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketSupport updated = service.updateAnswer(12, "Solución aplicada", null);

        assertThat(updated.getAnswer()).isEqualTo("Solución aplicada");
        assertThat(updated.getStatus()).isNotNull();
        assertThat(updated.getStatus().getIdTicketStatus()).isEqualTo(3);
    }

    @Test
    void updateStatus_sets_status_to_provided_id() {
        TicketSupport existing = new TicketSupport();
        existing.setIdTickets(13);
        when(ticketSupportRepository.findById(13)).thenReturn(Optional.of(existing));
        when(ticketStatusService.findById(4)).thenReturn(Optional.of(new TicketStatus() {{ setIdTicketStatus(4); setName("CLOSED"); }}));
        when(ticketSupportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicketSupport updated = service.updateStatus(13, 4);
        assertThat(updated.getStatus()).isNotNull();
        assertThat(updated.getStatus().getIdTicketStatus()).isEqualTo(4);
    }
}
