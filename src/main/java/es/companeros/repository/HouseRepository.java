package es.companeros.repository;

import es.companeros.model.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
    Optional<House> findByInvitationCode(String invitationCode);
}
