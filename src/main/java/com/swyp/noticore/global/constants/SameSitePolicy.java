package com.swyp.noticore.global.constants;

public enum SameSitePolicy {
    LAX("Lax"),
    STRICT("Strict"),
    NONE("None");

    private final String value;

    SameSitePolicy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
