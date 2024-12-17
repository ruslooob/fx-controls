package com.ruslooob.fxcontrols.filters.date;

import com.ruslooob.fxcontrols.filters.TextFilterType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

public final class DateBeforeFilter extends TextFilterType<LocalDate> {
    @Override
    public Function<String, Predicate<LocalDate>> createSearchFunction() {
        return search -> input -> {
            if (search == null || search.isBlank()) {
                return true;
            }
            LocalDate searchDate;
            try {
                searchDate = LocalDate.parse(search, dateFormatter);
            } catch (DateTimeParseException e) {
                return true; // ignore filter if wrong date passed
            }
            return input.isBefore(searchDate);
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
