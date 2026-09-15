package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :username ORDER BY a.timestamp DESC")
    Page<AuditLog> findByPerformedBy(@Param("username") String username, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") String entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.timestamp >= :since")
    long countByActionSince(@Param("action") AuditLog.AuditAction action, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM AuditLog a WHERE LOWER(a.performedBy) = LOWER(:username) AND LOWER(a.entityType) = LOWER(:entityType)")
    Page<AuditLog> findByPerformedByAndEntityType(@Param("username") String username, @Param("entityType") String entityType, Pageable pageable);
}
