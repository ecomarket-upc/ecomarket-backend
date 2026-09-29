package pe.edu.upc.ecomarket.shared.domain.model.valueobjects;

/**
 * Who is executing a command. Commands that modify owned resources carry it so the
 * application layer can enforce ownership (protection against IDOR).
 *
 * @param userId id of the authenticated user
 * @param admin  whether the user has the administrator role
 */
public record Requester(Long userId, boolean admin) {

    public Requester {
        if (userId == null) {
            throw new IllegalArgumentException("El usuario autenticado es obligatorio");
        }
    }

    /**
     * An administrator can manage anything; any other user only what they own.
     */
    public boolean canManage(Long ownerId) {
        return admin || userId.equals(ownerId);
    }
}
