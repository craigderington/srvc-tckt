package com.helpdesk.srvc_tckt.service;

import com.helpdesk.srvc_tckt.dto.TicketCreateRequest;
import com.helpdesk.srvc_tckt.dto.TicketUpdateRequest;
import com.helpdesk.srvc_tckt.entity.Ticket;
import com.helpdesk.srvc_tckt.repository.TicketRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    @Value("${pod.name:unknown-pod}")
    private String podName;

    @Value("${node.name:unknown-node}")
    private String nodeName;

    @Transactional
    public Ticket createTicket(TicketCreateRequest request) {
        String ticketNumber = generateTicketNumber();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .status(Ticket.Status.NEW)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .archived(false)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Created ticket {}", savedTicket.getTicketNumber());

        emailService.sendTicketCreatedEmail(savedTicket);

        return savedTicket;
    }

    @Transactional
    public Ticket updateTicket(Long id, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (request.getSubject() != null) {
            ticket.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignToAgent(Long ticketId, String agentName) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.getStatus() != Ticket.Status.NEW) {
            throw new RuntimeException("Only NEW tickets can be assigned. Current status: " + ticket.getStatus());
        }

        ticket.setAssignedToAgent(agentName);
        ticket.setHandledByPod(podName);
        ticket.setHandledByNode(nodeName);
        ticket.setStatus(Ticket.Status.ASSIGNED);
        ticket.setAssignedAt(ZonedDateTime.now());

        try {
            Ticket savedTicket = ticketRepository.save(ticket);
            log.info("Ticket {} assigned to {} on pod {} (node {})",
                    savedTicket.getTicketNumber(), agentName, podName, nodeName);

            emailService.sendTicketAssignedEmail(savedTicket);

            return savedTicket;
        } catch (OptimisticLockException e) {
            log.warn("Optimistic lock exception - ticket {} was already claimed by another agent",
                    ticket.getTicketNumber());
            throw new RuntimeException("This ticket was just claimed by another agent. Please select a different ticket.");
        }
    }

    @Transactional
    public Ticket startProgress(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.ASSIGNED && ticket.getStatus() != Ticket.Status.WAITING_CUSTOMER) {
            throw new RuntimeException("Can only start progress on ASSIGNED or WAITING_CUSTOMER tickets");
        }

        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket requestCustomerInfo(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.IN_PROGRESS) {
            throw new RuntimeException("Can only request info for IN_PROGRESS tickets");
        }

        ticket.setStatus(Ticket.Status.WAITING_CUSTOMER);
        Ticket savedTicket = ticketRepository.save(ticket);

        emailService.sendCustomerInfoRequestEmail(savedTicket);

        return savedTicket;
    }

    @Transactional
    public Ticket reopen(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.WAITING_CUSTOMER &&
            ticket.getStatus() != Ticket.Status.RESOLVED) {
            throw new RuntimeException("Can only reopen WAITING_CUSTOMER or RESOLVED tickets");
        }

        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket resolve(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.IN_PROGRESS) {
            throw new RuntimeException("Can only resolve IN_PROGRESS tickets");
        }

        ticket.setStatus(Ticket.Status.RESOLVED);
        ticket.setResolvedAt(ZonedDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        emailService.sendTicketResolvedEmail(savedTicket);

        return savedTicket;
    }

    @Transactional
    public Ticket close(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.RESOLVED) {
            throw new RuntimeException("Can only close RESOLVED tickets");
        }

        ticket.setStatus(Ticket.Status.CLOSED);

        Ticket savedTicket = ticketRepository.save(ticket);

        emailService.sendTicketClosedEmail(savedTicket);

        return savedTicket;
    }

    @Transactional
    public Ticket archive(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() != Ticket.Status.CLOSED) {
            throw new RuntimeException("Can only archive CLOSED tickets");
        }

        ticket.setArchived(true);
        ticket.setStatus(Ticket.Status.ARCHIVED);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllActiveTickets() {
        return ticketRepository.findByArchivedFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Ticket> getUnassignedTickets() {
        return ticketRepository.findByStatusAndArchivedFalseOrderByPriorityDescCreatedAtAsc(Ticket.Status.NEW);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByStatus(Ticket.Status status) {
        return ticketRepository.findByStatusAndArchivedFalseOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByPriority(Ticket.Priority priority) {
        return ticketRepository.findByPriorityAndArchivedFalseOrderByCreatedAtDesc(priority);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByCategory(Ticket.Category category) {
        return ticketRepository.findByCategoryAndArchivedFalseOrderByCreatedAtDesc(category);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByAgent(String agentName) {
        return ticketRepository.findByAssignedToAgentAndArchivedFalseOrderByCreatedAtDesc(agentName);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getArchivedTickets() {
        return ticketRepository.findByArchivedTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalActive = ticketRepository.findByArchivedFalseOrderByCreatedAtDesc().size();
        long totalArchived = ticketRepository.findByArchivedTrueOrderByCreatedAtDesc().size();

        stats.put("totalActive", totalActive);
        stats.put("totalArchived", totalArchived);

        Map<Ticket.Status, Long> statusCounts = ticketRepository.countByStatusGrouped().stream()
                .collect(Collectors.toMap(
                        arr -> (Ticket.Status) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("byStatus", statusCounts);

        Map<Ticket.Priority, Long> priorityCounts = ticketRepository.countByPriorityGrouped().stream()
                .collect(Collectors.toMap(
                        arr -> (Ticket.Priority) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("byPriority", priorityCounts);

        Map<Ticket.Category, Long> categoryCounts = ticketRepository.countByCategoryGrouped().stream()
                .collect(Collectors.toMap(
                        arr -> (Ticket.Category) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("byCategory", categoryCounts);

        Map<String, Long> podCounts = ticketRepository.countByPodGrouped().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("byPod", podCounts);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAgentStatistics(String agentName) {
        Map<String, Object> stats = new HashMap<>();

        List<Ticket> agentTickets = ticketRepository.findByAssignedToAgentAndArchivedFalseOrderByCreatedAtDesc(agentName);

        stats.put("totalAssigned", agentTickets.size());
        stats.put("resolved", agentTickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.RESOLVED || t.getStatus() == Ticket.Status.CLOSED)
                .count());
        stats.put("inProgress", agentTickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.IN_PROGRESS)
                .count());
        stats.put("waitingCustomer", agentTickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.WAITING_CUSTOMER)
                .count());

        OptionalDouble avgResolutionTime = agentTickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getAssignedAt() != null)
                .mapToLong(t -> java.time.Duration.between(t.getAssignedAt(), t.getResolvedAt()).toMinutes())
                .average();

        stats.put("avgResolutionTimeMinutes", avgResolutionTime.isPresent() ? avgResolutionTime.getAsDouble() : 0.0);
        stats.put("podName", podName);
        stats.put("nodeName", nodeName);

        return stats;
    }

    private String generateTicketNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "TKT-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }
}
