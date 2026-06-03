package com.agile.notificationservice.service;

import com.agile.notificationservice.dto.DeveloperOverloadEvent;
import com.agile.notificationservice.dto.RecipientDto;
import com.agile.notificationservice.dto.SprintOverloadEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender   mailSender;
    private final TemplateEngine   templateEngine;

    @Value("${mail.from}")
    private String fromAddress;

    public void sendSprintOverloadAlert(SprintOverloadEvent event) {
        for (RecipientDto recipient : event.recipients()) {
            try {
                Context ctx = new Context();
                ctx.setVariable("firstName",       recipient.firstName());
                ctx.setVariable("projectName",     event.projectName());
                ctx.setVariable("sprintName",      event.sprintName());
                ctx.setVariable("sprintCapacity",  event.sprintCapacity());
                ctx.setVariable("currentLoad",     event.currentLoad());
                ctx.setVariable("loadRate",
                    String.format("%.1f", event.loadRate()));
                ctx.setVariable("occurredAt",      event.occurredAt());

                String html = templateEngine.process("sprint-overload", ctx);

                sendEmail(
                    recipient.email(),
                    "[Alert] Sprint Overload — " + event.sprintName(),
                    html
                );
            } catch (Exception e) {
                log.error("Failed to send sprint overload email to {}: {}",
                    recipient.email(), e.getMessage());
                // Continue to next recipient — one failure must not block others
            }
        }
    }

    public void sendDeveloperOverloadAlert(DeveloperOverloadEvent event) {
        for (RecipientDto recipient : event.recipients()) {
            try {
                Context ctx = new Context();
                ctx.setVariable("firstName",         recipient.firstName());
                ctx.setVariable("projectName",       event.projectName());
                ctx.setVariable("sprintName",        event.sprintName());
                ctx.setVariable("developerFirstName",event.developerFirstName());
                ctx.setVariable("developerLastName", event.developerLastName());
                ctx.setVariable("developerLoad",     event.developerLoad());
                ctx.setVariable("developerCapacity", event.developerCapacity());
                ctx.setVariable("loadRate",
                    String.format("%.1f", event.loadRate()));
                ctx.setVariable("occurredAt",        event.occurredAt());

                String html = templateEngine.process("developer-overload", ctx);

                sendEmail(
                    recipient.email(),
                    "[Alert] Developer Overload — " + event.sprintName(),
                    html
                );
            } catch (Exception e) {
                log.error("Failed to send developer overload email to {}: {}",
                    recipient.email(), e.getMessage());
            }
        }
    }

    private void sendEmail(String to, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
        log.info("Email sent to {} — subject: {}", to, subject);
    }
}
