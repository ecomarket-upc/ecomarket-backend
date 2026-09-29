package pe.edu.upc.ecomarket.iam.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.iam.application.internal.outboundservices.hashing.HashingService;
import pe.edu.upc.ecomarket.iam.application.internal.outboundservices.tokens.TokenService;
import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;
import pe.edu.upc.ecomarket.iam.domain.model.commands.DeactivateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SeedAdminUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.ecomarket.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.ecomarket.iam.domain.services.UserCommandService;
import pe.edu.upc.ecomarket.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.ecomarket.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ConflictException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ForbiddenOperationException;
import pe.edu.upc.ecomarket.shared.domain.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    @Override
    @Transactional
    public AuthenticatedUser handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new ConflictException("Ya existe una cuenta con el correo " + command.email());
        }
        User user = new User(command.firstName(), command.lastName(), command.email(),
                hashingService.encode(command.password()), command.role());
        userRepository.save(user);
        return authenticate(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser handle(SignInCommand command) {
        User user = userRepository.findByEmail(command.email())
                .filter(found -> hashingService.matches(command.password(), found.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
        if (!user.isActive()) {
            throw new DisabledException("La cuenta está desactivada");
        }
        return authenticate(user);
    }

    @Override
    @Transactional
    public User handle(UpdateUserCommand command) {
        User user = findUser(command.userId());
        if (!command.requester().canManage(user.getId())) {
            throw new ForbiddenOperationException("Solo puedes editar tu propio perfil");
        }
        user.updateProfile(command.firstName(), command.lastName());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void handle(DeactivateUserCommand command) {
        User user = findUser(command.userId());
        if (user.getId().equals(command.requester().userId())) {
            throw new BusinessRuleException("Un administrador no puede desactivar su propia cuenta");
        }
        user.deactivate();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void handle(SeedAdminUserCommand command) {
        if (userRepository.existsByRole(Roles.ROLE_ADMIN)) {
            return;
        }
        userRepository.save(new User("Administrador", "EcoMarket", command.email().trim().toLowerCase(),
                hashingService.encode(command.password()), Roles.ROLE_ADMIN));
    }

    private AuthenticatedUser authenticate(User user) {
        return new AuthenticatedUser(user, tokenService.generateToken(user), tokenService.getExpirationInSeconds());
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
