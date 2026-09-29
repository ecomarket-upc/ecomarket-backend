package pe.edu.upc.ecomarket.iam.domain.model.queries;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

/**
 * @param requester only administrators or the same user may read a profile
 */
public record GetUserByIdQuery(Long userId, Requester requester) {
}
