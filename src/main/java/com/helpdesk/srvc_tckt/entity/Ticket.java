package com.helpdesk.srvc_tckt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketNumber;

    @NotBlank(message = "Subject is required")
    @Column(nullable = false)
    private String subject;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotBlank(message = "Customer name is required")
    @Column(nullable = false)
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Valid email is required")
    @Column(nullable = false)
    private String customerEmail;

    private String assignedToAgent;

    private String handledByPod;

    private String handledByNode;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    private ZonedDateTime assignedAt;

    private ZonedDateTime resolvedAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;

    @Version
    private Long version; // Optimistic locking

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        if (status == null) {
            status = Status.NEW;
        }
    }

    // Enums
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum Category {
        TECHNICAL, BILLING, ACCOUNT, GENERAL
    }

    public enum Status {
        NEW, ASSIGNED, IN_PROGRESS, WAITING_CUSTOMER, RESOLVED, CLOSED, ARCHIVED
    }
}
