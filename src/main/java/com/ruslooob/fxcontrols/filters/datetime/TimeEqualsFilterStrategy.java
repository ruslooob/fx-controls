package com.ruslooob.fxcontrols.filters.datetime;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.timeFormatter;

public final class TimeEqualsFilterStrategy extends TextFilterStrategy<LocalTime> {
    @Override
    public Function<String, Predicate<LocalTime>> createSearchFunction() {
        return search -> input -> {
            if (search == null || search.isBlank()) {
                return true;
            }
            LocalTime searchDate;
            try {
                searchDate = LocalTime.parse(search, timeFormatter);
            } catch (DateTimeParseException e) {
                return true; // ignore filter if wrong date passed
            }
            return input.equals(searchDate);
        };
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
