package es.companeros.model;

public enum Role {
    HOUSE_ADMIN("Administrador"),
    HOUSE_MEMBER("Miembro");

    private final String displayName;

    Role(String displayValue) {
        this.displayName = displayValue;
    }

    public String getDisplayName() {
        return displayName;
    }
}
