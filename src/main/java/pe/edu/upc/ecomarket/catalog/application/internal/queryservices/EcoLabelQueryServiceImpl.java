package pe.edu.upc.ecomarket.catalog.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllEcoLabelsQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetEcoLabelByIdQuery;
import pe.edu.upc.ecomarket.catalog.domain.services.EcoLabelQueryService;
import pe.edu.upc.ecomarket.catalog.infrastructure.persistence.jpa.repositories.EcoLabelRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EcoLabelQueryServiceImpl implements EcoLabelQueryService {

    private final EcoLabelRepository ecoLabelRepository;

    @Override
    public List<EcoLabel> handle(GetAllEcoLabelsQuery query) {
        return ecoLabelRepository.findAllByOrderByNameAsc();
    }

    @Override
    public EcoLabel handle(GetEcoLabelByIdQuery query) {
        return ecoLabelRepository.findById(query.ecoLabelId())
                .orElseThrow(() -> new ResourceNotFoundException("Eco-etiqueta", query.ecoLabelId()));
    }
}
