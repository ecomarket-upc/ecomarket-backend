package pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.ecomarket.iam.infrastructure.persistence.jpa.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
