package com.ruslooob.fxcontrols.filters.number;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.function.Function;
import java.util.function.Predicate;

public final class NumberBeforeFilterStrategy extends TextFilterStrategy<Number> {
    @Override
    public Function<String, Predicate<Number>> createSearchFunction() {
        return search -> input -> {
            if (search.isBlank()) {
                return true;
            }
            double searchNumber;
            try {
                searchNumber = Double.parseDouble(search);
            } catch (NumberFormatException e) {
                return false;
            }
            return input.doubleValue() < searchNumber;
        };
    }

    @Override
    public String toString() {
        return "<";
    }

    @Override
    public String getTooltipText() {
        return "Меньше";
    }
}
