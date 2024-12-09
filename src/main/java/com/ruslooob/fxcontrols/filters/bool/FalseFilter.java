package com.ruslooob.fxcontrols.filters.bool;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public final class FalseFilter extends TextFilterType<Boolean> {
    @Override
    public Function<String, Predicate<Boolean>> createSearchFunction() {
        return search -> input -> !input;
    }

    @Override
    public String toString() {
        return "Ложь";
    }
}
