package es.companeros.repository;

import es.companeros.model.House;
import es.companeros.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario a buscar.
     * @return Un Optional que contiene el usuario si se encuentra.
     */
    Optional<User> findByUsername(String username);

    List<User> findByHouse(House house);
}
