package com.rmtech.ecom.Service;

import com.rmtech.ecom.DTOS.AuditLogDto;
import com.rmtech.ecom.Entities.AuditLog;
import com.rmtech.ecom.Entities.AuditLog.AuditAction;
import com.rmtech.ecom.Repositories.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog createAuditLog(AuditAction action, String entityType, String entityId,
                                   String details, String performedBy, boolean success, String failureReason) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setPerformedBy(performedBy);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setSuccess(success);
        auditLog.setFailureReason(failureReason);

        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} - {}", action, entityId);
        return saved;
    }

    @Async("auditExecutor")
    public void logAsync(AuditAction action, String entityType, String entityId,
                         String details, String performedBy, boolean success, String failureReason) {
        try {
            createAuditLog(action, entityType, entityId, details, performedBy, success, failureReason);
        } catch (Exception e) {
            log.error("Failed to create audit log asynchronously", e);
        }
    }

    public Page<AuditLogDto> getUserAuditLogs(String username, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByPerformedBy(username, pageable);
        return logs.map(this::convertToDto);
    }

    public Page<AuditLogDto> getAuditLogsByEntity(String entityType, String entityId, Pageable pageable) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        Page<AuditLog> page = Page.empty(pageable);
        return page.map(this::convertToDto);
    }

    public Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByTimestampBetween(start, end, pageable);
        return logs.map(this::convertToDto);
    }

    public long getActionCount(AuditAction action, LocalDateTime since) {
        return auditLogRepository.countByActionSince(action, since);
    }

    public AuditLogDto convertToDto(AuditLog auditLog) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(auditLog.getId());
        dto.setAction(auditLog.getAction().name());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setDetails(auditLog.getDetails());
        dto.setPerformedBy(auditLog.getPerformedBy());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setSuccess(auditLog.getSuccess());
        dto.setFailureReason(auditLog.getFailureReason());
        return dto;
    }
}
