package com.ruslooob.fxcontrols.filters;

import com.ruslooob.fxcontrols.filters.date.DateEqualsFilter;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterType;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterType;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public sealed abstract class TextFilterType<T> permits SubstringFilterType, StartsWithFilterType, EqualsFilterType,
        DateEqualsFilter {
    public abstract Function<String, Predicate<T>> createSearchFunction();
}

