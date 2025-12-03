package com.helpdesk.srvc_tckt.dto;

import com.helpdesk.srvc_tckt.entity.Ticket;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private Ticket.Priority priority;

    @NotNull(message = "Category is required")
    private Ticket.Category category;

    @NotBlank(message = "Your name is required")
    private String customerName;

    @NotBlank(message = "Your email is required")
    @Email(message = "Please provide a valid email address")
    private String customerEmail;
}
