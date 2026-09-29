package pe.edu.upc.ecomarket.catalog.interfaces.rest.transform;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.EcoLabelResource;
import pe.edu.upc.ecomarket.catalog.interfaces.rest.resources.SaveEcoLabelResource;

import java.util.Set;

public final class EcoLabelAssemblers {

    private EcoLabelAssemblers() {
    }

    /**
     * @param ecoLabelId null to create, the id to update
     */
    public static SaveEcoLabelCommand toCommand(Long ecoLabelId, SaveEcoLabelResource resource) {
        return new SaveEcoLabelCommand(ecoLabelId, resource.name(), resource.description(), resource.criteria(),
                resource.keywords());
    }

    public static EcoLabelResource toResource(EcoLabel ecoLabel) {
        return new EcoLabelResource(ecoLabel.getId(), ecoLabel.getName(), ecoLabel.getDescription(),
                ecoLabel.getCriteria(), Set.copyOf(ecoLabel.getKeywords()));
    }
}
