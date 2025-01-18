package com.ruslooob.fxcontrols.filters.time;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.utils.Utils.timeFormatter;

public final class TimeBeforeFilterStrategy extends TextFilterStrategy<LocalTime> {
    @Override
    public Function<String, Predicate<LocalTime>> createSearchFunction() {
        return search -> input -> {
            if (search == null || search.isBlank()) {
                return true;
            }
            LocalTime searchTime;
            try {
                searchTime = LocalTime.parse(search, timeFormatter);
            } catch (DateTimeParseException e) {
                return false;
            }
            return input.truncatedTo(ChronoUnit.MINUTES).isBefore(searchTime.truncatedTo(ChronoUnit.MINUTES));
        };
    }

    @Override
    public String toString() {
        return "<";
    }

    @Override
    public String getTooltipText() {
        return "Раньше чем";
    }
}
