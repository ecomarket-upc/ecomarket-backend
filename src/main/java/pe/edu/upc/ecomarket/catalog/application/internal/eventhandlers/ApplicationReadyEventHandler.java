package pe.edu.upc.ecomarket.catalog.application.internal.eventhandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedCategoriesCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedEcoLabelsCommand;
import pe.edu.upc.ecomarket.catalog.domain.services.CategoryCommandService;
import pe.edu.upc.ecomarket.catalog.domain.services.EcoLabelCommandService;

/**
 * Loads the base categories and the official eco labels on the first start.
 */
@Component("catalogApplicationReadyEventHandler")
@RequiredArgsConstructor
public class ApplicationReadyEventHandler {

    private final CategoryCommandService categoryCommandService;
    private final EcoLabelCommandService ecoLabelCommandService;

    @EventListener
    public void on(ApplicationReadyEvent event) {
        categoryCommandService.handle(new SeedCategoriesCommand());
        ecoLabelCommandService.handle(new SeedEcoLabelsCommand());
    }
}
