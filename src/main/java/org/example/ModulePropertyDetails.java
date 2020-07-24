package org.example;

import java.util.List;

public class ModulePropertyDetails {
    private String name;
    private String type;
    private String defaultValue;
    /** This property is optional and will be used for special cases such as regexp and tokenSet. */
    private String validationType;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
