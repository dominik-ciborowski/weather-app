package com.weather.backend.model;

import java.util.Arrays;

public enum Unit {
    METRIC("metric"),
    IMPERIAL("imperial"),
    STANDARD("standard");

    private final String apiValue;

    Unit(String apiValue) {
        this.apiValue = apiValue;
    }

    public static Unit from(String value) {
        return Arrays.stream(values())
            .filter(unit -> unit.apiValue.equalsIgnoreCase(value) || unit.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported unit: " + value));
    }

    public String getApiValue() {
        return apiValue;
    }

}
