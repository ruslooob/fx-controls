package com.ruslooob.fxcontrols.filters.string;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public final class EqualsFilterType extends TextFilterType<String> {
    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> input.equalsIgnoreCase(search);
    }

    @Override
    public String toString() {
        return "=";
    }
}
