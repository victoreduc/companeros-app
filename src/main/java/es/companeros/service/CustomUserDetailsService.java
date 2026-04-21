package es.companeros.service;

import es.companeros.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio para cargar los detalles de un usuario para Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario a buscar.
     * @return Los detalles del usuario.
     * @throws UsernameNotFoundException Si no se encuentra el usuario.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario con el nombre de usuario: " + username));
    }
}
