package es.companeros.service;

import es.companeros.model.House;
import es.companeros.model.ShoppingListItem;
import es.companeros.repository.ShoppingListItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de la lista de la compra.
 */
@Service
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    public ShoppingListService(ShoppingListItemRepository shoppingListItemRepository) {
        this.shoppingListItemRepository = shoppingListItemRepository;
    }

    /**
     * Obtiene todos los artículos de la lista de la compra.
     * @return Lista de todos los artículos.
     */
    public List<ShoppingListItem> findAllItems() {
        return shoppingListItemRepository.findAll();
    }

    /**
     * Busca un artículo por casa.
     */
    public List<ShoppingListItem> findAllItemsByHouse(House house) {
        return shoppingListItemRepository.findByHouse(house); // Este método ya lo tienes en tu repositorio
    }
    public Optional<ShoppingListItem> findItemById(Long id) {
        return shoppingListItemRepository.findById(id);
    }

    /**
     * Guarda un nuevo artículo en la lista de la compra.
     * @param item El artículo a guardar.
     * @return El artículo guardado.
     */
    public ShoppingListItem saveItem(ShoppingListItem item) {
        return shoppingListItemRepository.save(item);
    }

    /**
     * Elimina un artículo de la lista de la compra.
     */
    public void deleteItem(Long id) {
        shoppingListItemRepository.deleteById(id);
    }

    public void clearByHouse(House house) {
        shoppingListItemRepository.deleteByHouse(house);
    }
}
