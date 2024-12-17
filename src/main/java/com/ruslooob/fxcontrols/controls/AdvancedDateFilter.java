package com.ruslooob.fxcontrols.controls;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedDateFilter extends AdvancedFilter<LocalDate> {
    // попробовать найти более удобный datepicker, чтобы можно было выбрать год, месяц и день сразу же
    DatePicker datePicker = new DatePicker();


    public AdvancedDateFilter() {
        getChildren().addAll(typeComboButton, datePicker);
        datePicker.setEditable(true);
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : dateFormatter.format(date);
            }

            @Override
            public LocalDate fromString(String str) {
                try {
                    return LocalDate.parse(str, dateFormatter);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalDate>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(datePicker.getEditor().getText()));
        });

        //also change predicate every time text in filter was changed
        datePicker.editorProperty().get().textProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalDate>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

}
