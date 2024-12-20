package com.ruslooob.fxcontrols.filters.datetime;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateTimeFormatter;

public final class DateTimeEqualsFilterStrategy extends TextFilterStrategy<LocalDateTime> {
    @Override
    public Function<String, Predicate<LocalDateTime>> createSearchFunction() {
        return search -> input -> {
            if (search == null || search.isBlank()) {
                return true;
            }
            LocalDateTime searchDate;
            try {
                searchDate = LocalDateTime.parse(search, dateTimeFormatter);
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
