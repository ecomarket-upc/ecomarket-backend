package pe.edu.upc.ecomarket.iam.domain.model.commands;

public record SignInCommand(String email, String password) {

    public SignInCommand {
        email = email.trim().toLowerCase();
    }
}
