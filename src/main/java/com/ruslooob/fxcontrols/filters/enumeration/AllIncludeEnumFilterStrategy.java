package com.ruslooob.fxcontrols.filters.enumeration;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.function.Function;
import java.util.function.Predicate;

public final class AllIncludeEnumFilterStrategy extends TextFilterStrategy<String> {
    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> true;
    }

    @Override
    public String toString() {
        return "Все";
    }
}
