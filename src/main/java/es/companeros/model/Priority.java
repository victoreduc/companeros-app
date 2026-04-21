package es.companeros.model;

public enum Priority {
    HIGH("Alta", "high"),
    MEDIUM("Media", "medium"),
    LOW("Baja", "low");

    private final String displayValue;
    private final String cssClass;

    Priority(String displayValue, String cssClass) {
        this.displayValue = displayValue;
        this.cssClass = cssClass;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getCssClass() {
        return cssClass;
    }
}
