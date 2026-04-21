package es.companeros.model;

public enum ExpenseStatus {
    PENDING("Pendiente"),
    PAID("Pagado");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
