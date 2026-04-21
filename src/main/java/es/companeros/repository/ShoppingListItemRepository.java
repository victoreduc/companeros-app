package es.companeros.repository;

import es.companeros.model.House;
import es.companeros.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repositorio para la entidad ShoppingListItem.
 */
@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByHouse(House house);

    @Transactional
    void deleteByHouse(House house);
}
