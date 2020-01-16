package api;

import javax.validation.constraints.Pattern;

public class Tag {
    public static final  String LABEL_PATTERN          = "[a-z0-9_\\-]+";
    private static final String ANCHORED_LABEL_PATTERN = "^" + LABEL_PATTERN + "$";

    @Pattern(regexp = ANCHORED_LABEL_PATTERN)
    private String label;
    private Long   id;

    public Tag() {
    }

    public Tag(@Pattern(regexp = ANCHORED_LABEL_PATTERN) String label) {
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
