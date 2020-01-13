package api;

import javax.validation.constraints.Pattern;

public class Tag {
    public static final String LABEL_PATTERN = "[a-z0-9_\\-]+";

    @Pattern(regexp = "^" + LABEL_PATTERN + "$")
    private String label;
    private Long id;

    public Tag() {
    }

    public Tag(@Pattern(regexp = "^[a-z0-9_\\-]+$") String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
