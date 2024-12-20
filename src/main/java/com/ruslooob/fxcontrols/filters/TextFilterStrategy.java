package com.ruslooob.fxcontrols.filters;

import com.ruslooob.fxcontrols.filters.date.DateAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.datetime.DateTimeEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.AllIncludeEnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.EnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterStrategy;

import java.util.function.Function;
import java.util.function.Predicate;

public sealed abstract class TextFilterStrategy<T> permits SubstringFilterStrategy, StartsWithFilterTypeStrategy, EqualsFilterTypeStrategy,
        DateEqualsFilterStrategy, DateBeforeFilterStrategy, DateAfterFilterStrategy,
        DateTimeEqualsFilterStrategy,
        NumberEqualsFilterStrategy, NumberBeforeFilterStrategy, NumberAfterFilterStrategy,
        EnumFilterStrategy, AllIncludeEnumFilterStrategy {
    public abstract Function<String, Predicate<T>> createSearchFunction();

    public abstract String toString();

    public String getTooltipText() {
        return "";
    }
}

