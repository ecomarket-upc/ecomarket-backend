package pe.edu.upc.ecomarket.iam.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.ecomarket.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

/**
 * Platform account. The email is the login name and the password is always stored as a BCrypt hash.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Roles role;

    @Column(nullable = false)
    private boolean active;

    public User(String firstName, String lastName, String email, String hashedPassword, Roles role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = hashedPassword;
        this.role = role;
        this.active = true;
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Logical delete: the account keeps its history but can no longer sign in.
     */
    public void deactivate() {
        this.active = false;
    }

    public boolean isAdmin() {
        return role == Roles.ROLE_ADMIN;
    }
}
