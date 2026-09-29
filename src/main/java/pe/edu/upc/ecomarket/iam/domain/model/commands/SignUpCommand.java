package pe.edu.upc.ecomarket.iam.domain.model.commands;

import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;

public record SignUpCommand(String firstName, String lastName, String email, String password, Roles role) {

    public SignUpCommand {
        if (role == null || !role.isSelfRegistrable()) {
            throw new IllegalArgumentException("Solo se pueden registrar cuentas ROLE_CONSUMER o ROLE_SELLER");
        }
        firstName = firstName.trim();
        lastName = lastName.trim();
        email = email.trim().toLowerCase();
    }
}
