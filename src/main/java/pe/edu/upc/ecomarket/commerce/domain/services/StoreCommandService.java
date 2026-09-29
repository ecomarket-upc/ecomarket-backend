package pe.edu.upc.ecomarket.commerce.domain.services;

import pe.edu.upc.ecomarket.commerce.domain.model.aggregates.Store;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.DeleteStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.ecomarket.commerce.domain.model.commands.ValidateStoreCommand;

public interface StoreCommandService {

    Store handle(CreateStoreCommand command);

    Store handle(UpdateStoreCommand command);

    void handle(DeleteStoreCommand command);

    Store handle(ValidateStoreCommand command);
}
