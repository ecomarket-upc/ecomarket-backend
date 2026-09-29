package pe.edu.upc.ecomarket.catalog.domain.services;

import pe.edu.upc.ecomarket.catalog.domain.model.aggregates.EcoLabel;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetAllEcoLabelsQuery;
import pe.edu.upc.ecomarket.catalog.domain.model.queries.GetEcoLabelByIdQuery;

import java.util.List;

public interface EcoLabelQueryService {

    List<EcoLabel> handle(GetAllEcoLabelsQuery query);

    EcoLabel handle(GetEcoLabelByIdQuery query);
}
