package pe.edu.upc.ecomarket.iam.domain.model.valueobjects;

/**
 * The three EcoMarket roles. Spring Security checks them with {@code hasRole('CONSUMER')},
 * {@code hasRole('SELLER')} and {@code hasRole('ADMIN')}.
 */
public enum Roles {
    ROLE_CONSUMER,
    ROLE_SELLER,
    ROLE_ADMIN;

    /**
     * Only consumers and sellers can create their own account; administrators are seeded.
     */
    public boolean isSelfRegistrable() {
        return this != ROLE_ADMIN;
    }
}
