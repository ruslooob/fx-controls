package com.ruslooob.fxcontrols.filters.enumeration;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public final class AllIncludeEnumFilter extends TextFilterType<String> {
    @Override
    public Function<String, Predicate<String>> createSearchFunction() {
        return search -> input -> true;
    }

    @Override
    public String toString() {
        return "Все";
    }
}
