package com.ruslooob.fxcontrols.filters;

import com.ruslooob.fxcontrols.filters.bool.FalseFilter;
import com.ruslooob.fxcontrols.filters.bool.TrueFilter;
import com.ruslooob.fxcontrols.filters.date.DateAfterFilter;
import com.ruslooob.fxcontrols.filters.date.DateBeforeFilter;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilter;
import com.ruslooob.fxcontrols.filters.number.NumberAfterFilter;
import com.ruslooob.fxcontrols.filters.number.NumberBeforeFilter;
import com.ruslooob.fxcontrols.filters.number.NumberEqualsFilter;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterType;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterType;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterType;

import java.util.function.Function;
import java.util.function.Predicate;

public sealed abstract class TextFilterType<T> permits SubstringFilterType, StartsWithFilterType, EqualsFilterType,
        DateEqualsFilter, DateBeforeFilter, DateAfterFilter,
        TrueFilter, FalseFilter,
        NumberEqualsFilter, NumberBeforeFilter, NumberAfterFilter {
    public abstract Function<String, Predicate<T>> createSearchFunction();

    public abstract String toString();

    public String getTooltipText() {
        return "";
    }
}

