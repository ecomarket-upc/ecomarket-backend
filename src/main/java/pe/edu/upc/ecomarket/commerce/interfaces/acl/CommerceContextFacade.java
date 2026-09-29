package pe.edu.upc.ecomarket.commerce.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * Anti-corruption layer that other bounded contexts (catalog) use to ask about stores
 * without depending on commerce internals.
 */
public interface CommerceContextFacade {

    /**
     * @return the id of the user who owns the store, or empty if the store does not exist
     */
    Optional<Long> fetchStoreOwnerId(Long storeId);

    boolean isStoreApproved(Long storeId);

    /**
     * @return ids of every approved store, the only ones whose products are public
     */
    List<Long> fetchApprovedStoreIds();

    /**
     * @return ids of the stores owned by the user
     */
    List<Long> fetchStoreIdsByOwnerId(Long ownerId);
}
