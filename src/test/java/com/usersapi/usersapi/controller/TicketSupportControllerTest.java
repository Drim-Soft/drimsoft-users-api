package com.usersapi.usersapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usersapi.usersapi.dto.TicketAnswerRequest;
import com.usersapi.usersapi.model.TicketStatus;
import com.usersapi.usersapi.model.TicketSupport;
import com.usersapi.usersapi.service.TicketSupportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketSupportControllerTest {

    private MockMvc mockMvc;
    private TicketSupportService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        service = Mockito.mock(TicketSupportService.class);
        TicketSupportController controller = new TicketSupportController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void updateStatus_returns_ok_when_service_succeeds() throws Exception {
        TicketSupport t = new TicketSupport();
        TicketStatus s = new TicketStatus(); s.setIdTicketStatus(2); s.setName("IN_PROGRESS");
        t.setStatus(s);
        Mockito.when(service.updateStatus(7, 2)).thenReturn(t);

        mockMvc.perform(patch("/tickets/7/status/2"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returns_404_when_not_found() throws Exception {
        Mockito.when(service.findById(999)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/tickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAnswer_returns_ok_when_valid() throws Exception {
        TicketSupport t = new TicketSupport();
        TicketStatus s = new TicketStatus(); s.setIdTicketStatus(3); s.setName("ANSWERED");
        t.setStatus(s);
        Mockito.when(service.updateAnswer(eq(10), eq("Respuesta"), any())).thenReturn(t);

        TicketAnswerRequest req = new TicketAnswerRequest();
        req.setAnswer("Respuesta");

        mockMvc.perform(patch("/tickets/10/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
