package com.helpdesk.srvc_tckt.controller;

import com.helpdesk.srvc_tckt.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TicketService ticketService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        String agentName = authentication != null ? authentication.getName() : "guest";

        Map<String, Object> stats = ticketService.getStatistics();
        model.addAttribute("stats", stats);
        model.addAttribute("agentName", agentName);

        if (authentication != null) {
            Map<String, Object> agentStats = ticketService.getAgentStatistics(agentName);
            model.addAttribute("agentStats", agentStats);
        }

        model.addAttribute("unassignedTickets", ticketService.getUnassignedTickets());

        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
