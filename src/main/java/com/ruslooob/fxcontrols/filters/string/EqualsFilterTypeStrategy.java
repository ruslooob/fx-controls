package com.ruslooob.fxcontrols.filters.string;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.function.Function;
import java.util.function.Predicate;

public final class EqualsFilterTypeStrategy extends TextFilterStrategy<String> {
    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> input.equalsIgnoreCase(search);
    }

    @Override
    public String toString() {
        return "=";
    }

    @Override
    public String getTooltipText() {
        return "Равно";
    }
}
