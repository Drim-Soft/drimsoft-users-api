package com.usersapi.usersapi.repository;

import com.usersapi.usersapi.model.TicketSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSupportRepository extends JpaRepository<TicketSupport, Integer> {
    List<TicketSupport> findByIdPlanifikaUser(Integer idPlanifikaUser);
    List<TicketSupport> findByDrimsoftUser_IdUser(Integer idUser);
}
