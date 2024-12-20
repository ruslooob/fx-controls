package com.ruslooob.fxcontrols.filters.string;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public final class StartsWithFilterTypeStrategy extends TextFilterStrategy<String> {
    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> input.toLowerCase(Locale.ROOT).startsWith(search.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "A...";
    }

    @Override
    public String getTooltipText() {
        return "Начинается с";
    }
}
