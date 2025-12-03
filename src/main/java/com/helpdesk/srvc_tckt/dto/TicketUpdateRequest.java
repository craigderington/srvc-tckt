package com.helpdesk.srvc_tckt.dto;

import com.helpdesk.srvc_tckt.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateRequest {

    private String subject;
    private String description;
    private Ticket.Priority priority;
    private Ticket.Category category;
}
