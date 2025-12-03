package com.helpdesk.srvc_tckt.controller;

import com.helpdesk.srvc_tckt.dto.TicketCreateRequest;
import com.helpdesk.srvc_tckt.dto.TicketUpdateRequest;
import com.helpdesk.srvc_tckt.entity.Ticket;
import com.helpdesk.srvc_tckt.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public String listTickets(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String value,
            Model model,
            Authentication authentication) {

        String agentName = authentication != null ? authentication.getName() : "guest";

        if (filter != null && value != null) {
            switch (filter) {
                case "status":
                    model.addAttribute("tickets", ticketService.getTicketsByStatus(Ticket.Status.valueOf(value)));
                    model.addAttribute("filterTitle", "Status: " + value);
                    break;
                case "priority":
                    model.addAttribute("tickets", ticketService.getTicketsByPriority(Ticket.Priority.valueOf(value)));
                    model.addAttribute("filterTitle", "Priority: " + value);
                    break;
                case "category":
                    model.addAttribute("tickets", ticketService.getTicketsByCategory(Ticket.Category.valueOf(value)));
                    model.addAttribute("filterTitle", "Category: " + value);
                    break;
                case "agent":
                    model.addAttribute("tickets", ticketService.getTicketsByAgent(value));
                    model.addAttribute("filterTitle", "Agent: " + value);
                    break;
                default:
                    model.addAttribute("tickets", ticketService.getAllActiveTickets());
            }
        } else {
            model.addAttribute("tickets", ticketService.getAllActiveTickets());
        }

        model.addAttribute("agentName", agentName);
        model.addAttribute("priorities", Ticket.Priority.values());
        model.addAttribute("categories", Ticket.Category.values());
        model.addAttribute("statuses", Ticket.Status.values());

        return "tickets/list";
    }

    @GetMapping("/queue")
    public String ticketQueue(Model model, Authentication authentication) {
        String agentName = authentication != null ? authentication.getName() : "guest";

        model.addAttribute("tickets", ticketService.getUnassignedTickets());
        model.addAttribute("agentName", agentName);
        model.addAttribute("pageTitle", "Ticket Queue (Unassigned)");

        return "tickets/queue";
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String agentName = authentication.getName();
        model.addAttribute("tickets", ticketService.getTicketsByAgent(agentName));
        model.addAttribute("agentName", agentName);
        model.addAttribute("agentStats", ticketService.getAgentStatistics(agentName));

        return "tickets/my-tickets";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, Authentication authentication) {
        String agentName = authentication != null ? authentication.getName() : "guest";

        Ticket ticket = ticketService.getTicketById(id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("agentName", agentName);

        return "tickets/detail";
    }

    @GetMapping("/new")
    public String newTicketForm(Model model) {
        model.addAttribute("ticketRequest", new TicketCreateRequest());
        model.addAttribute("priorities", Ticket.Priority.values());
        model.addAttribute("categories", Ticket.Category.values());

        return "tickets/create";
    }

    @PostMapping("/new")
    public String createTicket(
            @Valid @ModelAttribute("ticketRequest") TicketCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("categories", Ticket.Category.values());
            return "tickets/create";
        }

        try {
            Ticket ticket = ticketService.createTicket(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " created successfully!");
            return "redirect:/tickets/" + ticket.getId();
        } catch (Exception e) {
            log.error("Error creating ticket", e);
            model.addAttribute("errorMessage", "Error creating ticket: " + e.getMessage());
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("categories", Ticket.Category.values());
            return "tickets/create";
        }
    }

    @PostMapping("/{id}/assign")
    public String assignTicket(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            String agentName = authentication.getName();
            Ticket ticket = ticketService.assignToAgent(id, agentName);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " assigned to you!");
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error assigning ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/start")
    public String startProgress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.startProgress(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Started working on ticket " + ticket.getTicketNumber());
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error starting progress", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/request-info")
    public String requestInfo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.requestCustomerInfo(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Requested additional information from customer for ticket " + ticket.getTicketNumber());
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error requesting info", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/reopen")
    public String reopenTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.reopen(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reopened ticket " + ticket.getTicketNumber());
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error reopening ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/resolve")
    public String resolveTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.resolve(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " marked as resolved!");
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error resolving ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/close")
    public String closeTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.close(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " closed!");
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            log.error("Error closing ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @PostMapping("/{id}/archive")
    public String archiveTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticket.getTicketNumber() + " archived!");
            return "redirect:/tickets";
        } catch (Exception e) {
            log.error("Error archiving ticket", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tickets/" + id;
        }
    }

    @GetMapping("/stats")
    public String statistics(Model model) {
        Map<String, Object> stats = ticketService.getStatistics();
        model.addAttribute("stats", stats);

        return "tickets/statistics";
    }
}
