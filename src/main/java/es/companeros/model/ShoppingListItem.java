package es.companeros.model;

import javax.persistence.*;

/**
 * Representa un artículo en la lista de la compra.
 */
@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean purchased = false;

    private Long addedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public Long getAddedByUserId() {
        return addedByUserId;
    }

    public void setAddedByUserId(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }
}
