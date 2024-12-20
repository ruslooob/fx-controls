package com.ruslooob.fxcontrols.column_utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CellFactories {
    public static <S> Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> createDateCellFactory(DateTimeFormatter dateFormatter) {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(dateFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        };
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(converter.toString(item));
                }
            }
        };
    }

    public static <S> Callback<TableColumn<S, Boolean>, TableCell<S, Boolean>> createBooleanCellFactory() {
        return createBooleanCellFactory(List.of("Да", "Нет"));
    }

    public static <S> Callback<TableColumn<S, Boolean>, TableCell<S, Boolean>> createBooleanCellFactory(List<String> booleanStrs) {
        if (booleanStrs.size() != 2) {
            throw new IllegalArgumentException("Boolean variations must have size 2. Given %s".formatted(booleanStrs));
        }
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean isEmployed, boolean empty) {
                super.updateItem(isEmployed, empty);
                if (empty || isEmployed == null) {
                    setText(null);
                } else {
                    setText(isEmployed ? booleanStrs.get(0) : booleanStrs.get(1));
                }
            }
        };
    }
}
