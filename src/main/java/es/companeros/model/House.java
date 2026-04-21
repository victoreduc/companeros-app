package es.companeros.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "houses")
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true)
    private String invitationCode;

    private boolean shoppingEnabled = true;
    private boolean expensesEnabled = true;

    @OneToMany(mappedBy = "house")
    private Set<User> members = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (invitationCode == null) {
            invitationCode = UUID.randomUUID().toString().substring(0, 8);
        }
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public boolean isShoppingEnabled() { return shoppingEnabled; }
    public void setShoppingEnabled(boolean shoppingEnabled) { this.shoppingEnabled = shoppingEnabled; }

    public boolean isExpensesEnabled() { return expensesEnabled; }
    public void setExpensesEnabled(boolean expensesEnabled) { this.expensesEnabled = expensesEnabled; }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }
}
