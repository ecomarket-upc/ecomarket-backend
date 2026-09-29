package pe.edu.upc.ecomarket.iam.application.internal.eventhandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SeedAdminUserCommand;
import pe.edu.upc.ecomarket.iam.domain.services.UserCommandService;

/**
 * Nobody can self-register as administrator, so one is created at startup from
 * ADMIN_EMAIL / ADMIN_PASSWORD when none exists yet.
 */
@Component("iamApplicationReadyEventHandler")
@RequiredArgsConstructor
public class ApplicationReadyEventHandler {

    private final UserCommandService userCommandService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @EventListener
    public void on(ApplicationReadyEvent event) {
        userCommandService.handle(new SeedAdminUserCommand(adminEmail, adminPassword));
    }
}
