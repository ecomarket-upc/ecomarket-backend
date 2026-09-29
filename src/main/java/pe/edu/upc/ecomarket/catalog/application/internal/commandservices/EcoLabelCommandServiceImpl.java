package pe.edu.upc.ecomarket.catalog.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.DeleteEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SaveEcoLabelCommand;
import pe.edu.upc.ecomarket.catalog.domain.model.commands.SeedEcoLabelsCommand;
import pe.edu.upc.ecomarket.catalog.domain.services.EcoLabelCommandService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.EcoLabelRepository;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ConflictException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EcoLabelCommandServiceImpl implements EcoLabelCommandService {

    private static final List<EcoLabel> BASE_ECO_LABELS = List.of(
            new EcoLabel("Venta a granel", "Se vende por peso, sin empaque individual",
                    "El cliente lleva su envase o compra la cantidad exacta que necesita",
                    Set.of("granel", "por kilo", "por peso", "rellenable", "recarga", "trae tu envase")),
            new EcoLabel("Sin envase plástico", "No usa plástico de un solo uso en su empaque",
                    "Empaque de vidrio, papel, cartón, tela o sin empaque",
                    Set.of("sin plastico", "libre de plastico", "sin empaque", "vidrio", "papel kraft",
                            "carton", "zero waste", "cero residuos")),
            new EcoLabel("Producto local", "Producido en Perú por productores cercanos",
                    "Elaborado o cultivado por productores nacionales, idealmente de la región",
                    Set.of("local", "peruano", "hecho en peru", "productor", "chacra", "comunidad", "andino")),
            new EcoLabel("Artesanal", "Elaborado a mano o en pequeña escala",
                    "Producción manual o en lotes pequeños, sin procesos industriales",
                    Set.of("artesanal", "hecho a mano", "tejido a mano", "casero", "elaboracion propia")),
            new EcoLabel("Orgánico", "Cultivado sin pesticidas ni fertilizantes sintéticos",
                    "Producción orgánica o agroecológica",
                    Set.of("organico", "sin pesticidas", "agroecologico", "sin quimicos", "natural")),
            new EcoLabel("Reutilizable", "Pensado para usarse muchas veces",
                    "Reemplaza a un producto desechable",
                    Set.of("reutilizable", "lavable", "retornable", "durable", "recargable")),
            new EcoLabel("Biodegradable", "Se descompone de forma natural",
                    "Material compostable o de origen vegetal",
                    Set.of("biodegradable", "compostable", "bambu", "fibra natural", "algodon organico")));

    private final EcoLabelRepository ecoLabelRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public EcoLabel handle(SaveEcoLabelCommand command) {
        String name = command.name().trim();
        if (command.ecoLabelId() == null) {
            if (ecoLabelRepository.existsByNameIgnoreCase(name)) {
                throw new ConflictException("Ya existe una eco-etiqueta llamada " + name);
            }
            return ecoLabelRepository.save(new EcoLabel(name, command.description(), command.criteria(), command.keywords()));
        }
        EcoLabel ecoLabel = ecoLabelRepository.findById(command.ecoLabelId())
                .orElseThrow(() -> new ResourceNotFoundException("Eco-etiqueta", command.ecoLabelId()));
        if (ecoLabelRepository.existsByNameIgnoreCaseAndIdNot(name, ecoLabel.getId())) {
            throw new ConflictException("Ya existe una eco-etiqueta llamada " + name);
        }
        ecoLabel.update(name, command.description(), command.criteria(), command.keywords());
        return ecoLabelRepository.save(ecoLabel);
    }

    /**
     * The label is also removed from every product that carried it.
     */
    @Override
    @Transactional
    public void handle(DeleteEcoLabelCommand command) {
        EcoLabel ecoLabel = ecoLabelRepository.findById(command.ecoLabelId())
                .orElseThrow(() -> new ResourceNotFoundException("Eco-etiqueta", command.ecoLabelId()));
        productRepository.findAllByEcoLabelId(ecoLabel.getId())
                .forEach(product -> product.removeEcoLabel(ecoLabel.getId()));
        ecoLabelRepository.delete(ecoLabel);
    }

    @Override
    @Transactional
    public void handle(SeedEcoLabelsCommand command) {
        if (ecoLabelRepository.count() > 0) {
            return;
        }
        BASE_ECO_LABELS.forEach(base -> ecoLabelRepository.save(
                new EcoLabel(base.getName(), base.getDescription(), base.getCriteria(), base.getKeywords())));
    }
}
