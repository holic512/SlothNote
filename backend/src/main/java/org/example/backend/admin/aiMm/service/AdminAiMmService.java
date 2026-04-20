package org.example.backend.admin.aiMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.aiMm.dto.AdminAiSessionDetailDto;
import org.example.backend.admin.aiMm.dto.AdminAiSessionRowDto;
import org.example.backend.admin.aiMm.request.SearchAiSessionRequest;
import org.example.backend.admin.repository.AdminUserRepository;
import org.example.backend.common.entity.AiChatSession;
import org.example.backend.common.entity.User;
import org.example.backend.user.ai.dto.AiChatMessageDto;
import org.example.backend.user.ai.dto.ContextNoteDto;
import org.example.backend.user.ai.repository.AiChatMessageRepository;
import org.example.backend.user.ai.repository.AiChatSessionNoteRefRepository;
import org.example.backend.user.ai.repository.AiChatSessionRepository;
import org.example.backend.user.ai.service.UserAiToolService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminAiMmService {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final AiChatSessionNoteRefRepository noteRefRepository;
    private final UserAiToolService userAiToolService;
    private final AdminUserRepository adminUserRepository;

    public AdminAiMmService(AiChatSessionRepository sessionRepository,
                            AiChatMessageRepository messageRepository,
                            AiChatSessionNoteRefRepository noteRefRepository,
                            UserAiToolService userAiToolService,
                            AdminUserRepository adminUserRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.noteRefRepository = noteRefRepository;
        this.userAiToolService = userAiToolService;
        this.adminUserRepository = adminUserRepository;
    }

    public long getCount() {
        return sessionRepository.count();
    }

    public List<AdminAiSessionRowDto> search(SearchAiSessionRequest request) {
        Page<AiChatSession> page = sessionRepository.findAll(
                buildSpec(request),
                PageRequest.of(Math.max(request.getPageNum() - 1, 0), Math.max(request.getPageSize(), 1), Sort.by(Sort.Direction.DESC, "lastMessageAt"))
        );
        return toRows(page.getContent());
    }

    public long countSearch(SearchAiSessionRequest request) {
        return sessionRepository.count(buildSpec(request));
    }

    public AdminAiSessionDetailDto detail(Long id) {
        Optional<AiChatSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return null;
        }
        AiChatSession session = sessionOpt.get();
        AdminAiSessionRowDto row = toRow(session);
        List<AiChatMessageDto> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(id).stream()
                .map(message -> new AiChatMessageDto(
                        message.getId(),
                        message.getRole(),
                        message.getMessageType(),
                        message.getContentMd(),
                        message.getStatus(),
                        message.getCreatedAt()
                ))
                .toList();
        List<ContextNoteDto> contextNotes = noteRefRepository.findBySessionIdOrderBySortOrderAscCreatedAtAsc(id).stream()
                .map(ref -> userAiToolService.readNoteMeta(session.getUserId(), ref.getNoteId()))
                .filter(Objects::nonNull)
                .toList();
        return new AdminAiSessionDetailDto(row, messages, contextNotes);
    }

    public boolean delete(Long id) {
        Optional<AiChatSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        AiChatSession session = sessionOpt.get();
        session.setIsDeleted(1);
        sessionRepository.save(session);
        return true;
    }

    public boolean restore(Long id) {
        Optional<AiChatSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        AiChatSession session = sessionOpt.get();
        session.setIsDeleted(0);
        sessionRepository.save(session);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<AiChatSession> sessions = sessionRepository.findAllById(ids);
            if (sessions.isEmpty()) {
                return false;
            }
            sessions.forEach(session -> session.setIsDeleted(1));
            sessionRepository.saveAll(sessions);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<AiChatSession> sessions = sessionRepository.findAllById(ids);
            if (sessions.isEmpty()) {
                return false;
            }
            sessions.forEach(session -> session.setIsDeleted(0));
            sessionRepository.saveAll(sessions);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private List<AdminAiSessionRowDto> toRows(List<AiChatSession> sessions) {
        return sessions.stream().map(this::toRow).toList();
    }

    private AdminAiSessionRowDto toRow(AiChatSession session) {
        AdminAiSessionRowDto dto = new AdminAiSessionRowDto();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setTitle(session.getTitle());
        dto.setIsDeleted(session.getIsDeleted());
        dto.setLastMessageAt(session.getLastMessageAt());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        User user = resolveUser(session);
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
        }
        dto.setMessageCount(messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).size());
        dto.setContextNoteCount(noteRefRepository.findBySessionIdOrderBySortOrderAscCreatedAtAsc(session.getId()).size());
        return dto;
    }

    private User resolveUser(AiChatSession session) {
        return adminUserRepository.findById(session.getUserId()).orElse(null);
    }

    private Specification<AiChatSession> buildSpec(SearchAiSessionRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getQ() != null && !request.getQ().trim().isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + request.getQ().trim() + "%"));
            }
            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), request.getUserId()));
            }
            if (request.getIsDeleted() != null) {
                predicates.add(cb.equal(root.get("isDeleted"), request.getIsDeleted()));
            }
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                var userRoot = subquery.from(User.class);
                subquery.select(userRoot.get("id"))
                        .where(cb.like(userRoot.get("username"), "%" + request.getUsername().trim() + "%"));
                predicates.add(root.get("userId").in(subquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
