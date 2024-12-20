package com.ruslooob.fxcontrols.filters.enumeration;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.function.Function;
import java.util.function.Predicate;

public final class EnumFilterStrategy extends TextFilterStrategy<String> {
    private final String value;

    public EnumFilterStrategy(String value) {
        this.value = value;
    }

    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> input.equalsIgnoreCase(value);
    }

    @Override
    public String toString() {
        return value;
    }
}