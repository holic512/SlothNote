package org.example.backend.common.repository;

import jakarta.transaction.Transactional;
import org.example.backend.common.entity.AuthTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTicketRepository extends JpaRepository<AuthTicket, Long> {

    Optional<AuthTicket> findByTicketId(String ticketId);

    @Modifying
    @Transactional
    @Query("delete from AuthTicket t where t.ticketType = :ticketType and t.subjectKey = :subjectKey and t.usedAt is null")
    void deleteActiveByTypeAndSubjectKey(@Param("ticketType") String ticketType, @Param("subjectKey") String subjectKey);

    @Modifying
    @Transactional
    @Query("delete from AuthTicket t where t.expireAt < :now or t.usedAt is not null")
    void deleteExpiredOrUsed(@Param("now") LocalDateTime now);
}
