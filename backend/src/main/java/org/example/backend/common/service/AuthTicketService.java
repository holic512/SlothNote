package org.example.backend.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.entity.AuthTicket;
import org.example.backend.common.repository.AuthTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthTicketService {

    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USER_REGISTER = "USER_REGISTER";
    public static final String USER_REGISTER_PENDING = "USER_REGISTER_PENDING";
    public static final String ADMIN_LOGIN = "ADMIN_LOGIN";

    private final AuthTicketRepository authTicketRepository;
    private final ObjectMapper objectMapper;

    public AuthTicketService(AuthTicketRepository authTicketRepository) {
        this.authTicketRepository = authTicketRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public AuthTicket createTicket(String ticketType,
                                   String subjectKey,
                                   String ticketId,
                                   String code,
                                   Object payload,
                                   int timeoutMinutes) throws JsonProcessingException {
        authTicketRepository.deleteExpiredOrUsed(LocalDateTime.now());
        authTicketRepository.deleteActiveByTypeAndSubjectKey(ticketType, subjectKey);

        AuthTicket authTicket = new AuthTicket();
        authTicket.setTicketId(ticketId);
        authTicket.setTicketType(ticketType);
        authTicket.setSubjectKey(subjectKey);
        authTicket.setCode(code);
        authTicket.setPayloadJson(objectMapper.writeValueAsString(payload));
        authTicket.setExpireAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        return authTicketRepository.save(authTicket);
    }

    @Transactional(readOnly = true)
    public Optional<AuthTicket> findValidTicket(String ticketId, String ticketType) {
        return authTicketRepository.findByTicketId(ticketId)
                .filter(ticket -> ticketType.equals(ticket.getTicketType()))
                .filter(ticket -> ticket.getUsedAt() == null)
                .filter(ticket -> ticket.getExpireAt().isAfter(LocalDateTime.now()));
    }

    @Transactional
    public void markUsed(AuthTicket ticket) {
        ticket.setUsedAt(LocalDateTime.now());
        authTicketRepository.save(ticket);
    }

    public Map<String, String> readStringMap(AuthTicket ticket) throws JsonProcessingException {
        return objectMapper.readValue(ticket.getPayloadJson(), new TypeReference<>() {});
    }

    public String writePayload(Object payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload);
    }

    public Map<String, Object> readObjectMap(AuthTicket ticket) throws JsonProcessingException {
        return objectMapper.readValue(ticket.getPayloadJson(), new TypeReference<>() {});
    }

    @Transactional
    public void replacePendingRegisterKeys(String username, String email, String regId, int timeoutMinutes) throws JsonProcessingException {
        createTicket(USER_REGISTER_PENDING, username, regId, regId, Collections.singletonMap("regId", regId), timeoutMinutes);
        createTicket(USER_REGISTER_PENDING, email, regId, regId, Collections.singletonMap("regId", regId), timeoutMinutes);
    }
}
