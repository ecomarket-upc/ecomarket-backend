package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedEcoLabelsCommand;

public interface EcoLabelCommandService {

    EcoLabel handle(SaveEcoLabelCommand command);

    void handle(DeleteEcoLabelCommand command);

    void handle(SeedEcoLabelsCommand command);
}
