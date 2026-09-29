package pe.edu.upc.ecomarket.commerce.domain.services;

import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetApprovedStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetNearbyStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetPendingStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetStoresByOwnerIdQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.NearbyStore;

import java.util.List;

public interface StoreQueryService {

    List<Store> handle(GetApprovedStoresQuery query);

    Store handle(GetStoreByIdQuery query);

    List<Store> handle(GetStoresByOwnerIdQuery query);

    List<Store> handle(GetPendingStoresQuery query);

    List<NearbyStore> handle(GetNearbyStoresQuery query);
}
