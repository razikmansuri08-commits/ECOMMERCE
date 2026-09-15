package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private String link;

    public enum NotificationType {
        ORDER_CONFIRMATION,
        ORDER_STATUS_UPDATE,
        ORDER_CANCELLED,
        LOW_STOCK,
        ACCOUNT_UPDATE,
        SYSTEM
    }

    public enum NotificationStatus {
        UNREAD,
        READ,
        DISMISSED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
