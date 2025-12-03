package com.helpdesk.srvc_tckt.service;

import com.helpdesk.srvc_tckt.entity.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Async
    public void sendTicketCreatedEmail(Ticket ticket) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent ticket created email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Ticket Created: " + ticket.getTicketNumber());
            message.setText(String.format("""
                Hello %s,

                Your support ticket has been created successfully.

                Ticket Number: %s
                Subject: %s
                Priority: %s
                Category: %s
                Status: %s

                We will review your ticket and assign it to an agent shortly.
                You will receive email updates as your ticket progresses.

                Thank you for contacting support!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getStatus()
            ));

            mailSender.send(message);
            log.info("Sent ticket created email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send ticket created email", e);
        }
    }

    @Async
    public void sendTicketAssignedEmail(Ticket ticket) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent ticket assigned email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Ticket Assigned: " + ticket.getTicketNumber());
            message.setText(String.format("""
                Hello %s,

                Your support ticket has been assigned to an agent.

                Ticket Number: %s
                Subject: %s
                Assigned To: %s
                Status: %s

                Your assigned agent will begin working on your issue shortly.

                Thank you for your patience!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getAssignedToAgent(),
                ticket.getStatus()
            ));

            mailSender.send(message);
            log.info("Sent ticket assigned email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send ticket assigned email", e);
        }
    }

    @Async
    public void sendTicketStatusUpdateEmail(Ticket ticket, Ticket.Status oldStatus, String comment) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent status update email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Ticket Update: " + ticket.getTicketNumber());

            String commentSection = (comment != null && !comment.isBlank())
                ? "\nAgent Comment: " + comment + "\n"
                : "";

            message.setText(String.format("""
                Hello %s,

                Your support ticket has been updated.

                Ticket Number: %s
                Subject: %s
                Previous Status: %s
                New Status: %s
                %s
                We will continue to work on your ticket and keep you informed.

                Thank you!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                oldStatus,
                ticket.getStatus(),
                commentSection
            ));

            mailSender.send(message);
            log.info("Sent status update email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send status update email", e);
        }
    }

    @Async
    public void sendTicketResolvedEmail(Ticket ticket) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent ticket resolved email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Ticket Resolved: " + ticket.getTicketNumber());
            message.setText(String.format("""
                Hello %s,

                Great news! Your support ticket has been resolved.

                Ticket Number: %s
                Subject: %s
                Resolved By: %s

                If you're satisfied with the resolution, this ticket will be automatically closed.
                If you need further assistance, please reply to this email or create a new ticket.

                Thank you for using our support services!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getAssignedToAgent()
            ));

            mailSender.send(message);
            log.info("Sent ticket resolved email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send ticket resolved email", e);
        }
    }

    @Async
    public void sendCustomerInfoRequestEmail(Ticket ticket) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent info request email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Additional Information Needed: " + ticket.getTicketNumber());
            message.setText(String.format("""
                Hello %s,

                Our support agent needs additional information to resolve your ticket.

                Ticket Number: %s
                Subject: %s
                Agent: %s

                Please reply to this email with the requested information so we can continue
                working on your ticket.

                Thank you for your cooperation!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getAssignedToAgent()
            ));

            mailSender.send(message);
            log.info("Sent info request email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send info request email", e);
        }
    }

    @Async
    public void sendTicketClosedEmail(Ticket ticket) {
        if (!emailEnabled) {
            log.info("Email disabled. Would have sent ticket closed email for: {}", ticket.getTicketNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ticket.getCustomerEmail());
            message.setSubject("Ticket Closed: " + ticket.getTicketNumber());
            message.setText(String.format("""
                Hello %s,

                Your support ticket has been closed.

                Ticket Number: %s
                Subject: %s
                Resolved By: %s

                We hope we were able to help you with your issue.
                If you need further assistance, please create a new ticket.

                Thank you for using our support services!

                ---
                HelpDesk Support Team
                """,
                ticket.getCustomerName(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getAssignedToAgent()
            ));

            mailSender.send(message);
            log.info("Sent ticket closed email to: {}", ticket.getCustomerEmail());
        } catch (Exception e) {
            log.error("Failed to send ticket closed email", e);
        }
    }
}
