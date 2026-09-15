package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.AuditLogDto;
import com.rmtech.ecom.Service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class Audit_Controller {

    private final AuditLogService auditLogService;

    public Audit_Controller(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<AuditLogDto>> getUserAuditLogs(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogDto> logs = auditLogService.getUserAuditLogs(username, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (start == null) {
            start = LocalDateTime.now().minusDays(7);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        
        Page<AuditLogDto> logs = auditLogService.getAuditLogsByDateRange(start, end, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action-count")
    public ResponseEntity<Long> getActionCount(
            @RequestParam String action,
            @RequestParam(required = false) LocalDateTime since) {
        
        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }
        
        long count = auditLogService.getActionCount(
                com.rmtech.ecom.Entities.AuditLog.AuditAction.valueOf(action),
                since
        );
        
        return ResponseEntity.ok(count);
    }
}
