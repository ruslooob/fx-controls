package com.ruslooob.fxcontrols.filters.date;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

public final class DateAfterFilter extends TextFilterType<LocalDate> {
    @Override
    public Function<String, Predicate<LocalDate>> createSearchFunction() {
        return search -> input -> {
            if (search.isBlank()) {
                return true;
            }
            LocalDate searchDate;
            try {
                searchDate = LocalDate.parse(search, dateFormatter);
            } catch (DateTimeParseException e) {
                return false;
            }
            return input.isAfter(searchDate);
        };
    }

    @Override
    public String toString() {
        return ">";
    }
}
