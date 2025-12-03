package com.helpdesk.srvc_tckt.repository;

import com.helpdesk.srvc_tckt.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by status
    List<Ticket> findByStatusAndArchivedFalseOrderByCreatedAtDesc(Ticket.Status status);

    // Find tickets by priority
    List<Ticket> findByPriorityAndArchivedFalseOrderByCreatedAtDesc(Ticket.Priority priority);

    // Find tickets by category
    List<Ticket> findByCategoryAndArchivedFalseOrderByCreatedAtDesc(Ticket.Category category);

    // Find tickets assigned to an agent
    List<Ticket> findByAssignedToAgentAndArchivedFalseOrderByCreatedAtDesc(String agentName);

    // Find all active (non-archived) tickets
    List<Ticket> findByArchivedFalseOrderByCreatedAtDesc();

    // Find archived tickets
    List<Ticket> findByArchivedTrueOrderByCreatedAtDesc();

    // Find tickets by customer email
    List<Ticket> findByCustomerEmailOrderByCreatedAtDesc(String email);

    // Count tickets by status
    long countByStatusAndArchivedFalse(Ticket.Status status);

    // Count tickets by priority
    long countByPriorityAndArchivedFalse(Ticket.Priority priority);

    // Count tickets by category
    long countByCategoryAndArchivedFalse(Ticket.Category category);

    // Count tickets by agent
    long countByAssignedToAgentAndArchivedFalse(String agentName);

    // Statistics query - count by status
    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.archived = false GROUP BY t.status")
    List<Object[]> countByStatusGrouped();

    // Statistics query - count by priority
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.archived = false GROUP BY t.priority")
    List<Object[]> countByPriorityGrouped();

    // Statistics query - count by category
    @Query("SELECT t.category, COUNT(t) FROM Ticket t WHERE t.archived = false GROUP BY t.category")
    List<Object[]> countByCategoryGrouped();

    // Statistics query - count by pod
    @Query("SELECT t.handledByPod, COUNT(t) FROM Ticket t WHERE t.archived = false AND t.handledByPod IS NOT NULL GROUP BY t.handledByPod")
    List<Object[]> countByPodGrouped();

    // Find unassigned tickets (NEW status)
    List<Ticket> findByStatusAndArchivedFalseOrderByPriorityDescCreatedAtAsc(Ticket.Status status);
}
