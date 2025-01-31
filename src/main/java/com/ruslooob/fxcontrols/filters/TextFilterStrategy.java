package com.ruslooob.fxcontrols.filters;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class TextFilterStrategy<T> {
    public abstract Function<String, Predicate<T>> createSearchFunction();

    public abstract String toString();

    public String getTooltipText() {
        return "";
    }
}

