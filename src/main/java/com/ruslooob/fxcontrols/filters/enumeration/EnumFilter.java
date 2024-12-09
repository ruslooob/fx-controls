package com.ruslooob.fxcontrols.filters.enumeration;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public final class EnumFilter extends TextFilterType<String> {
    private final String value;

    public EnumFilter(String value) {
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