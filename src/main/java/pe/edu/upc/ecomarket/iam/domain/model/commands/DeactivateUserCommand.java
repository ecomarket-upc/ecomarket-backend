package pe.edu.upc.ecomarket.iam.domain.model.commands;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

public record DeactivateUserCommand(Long userId, Requester requester) {
}
