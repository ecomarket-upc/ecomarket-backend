package pe.edu.upc.ecomarket.iam.domain.model.commands;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

public record UpdateUserCommand(Long userId, String firstName, String lastName, Requester requester) {

    public UpdateUserCommand {
        firstName = firstName.trim();
        lastName = lastName.trim();
    }
}
