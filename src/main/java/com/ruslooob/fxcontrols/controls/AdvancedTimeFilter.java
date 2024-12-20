package com.ruslooob.fxcontrols.controls;

import javafx.scene.control.TextField;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedTimeFilter extends AdvancedFilter<LocalTime> {
    //todo make stepper for time control
    // separate date and separate time
    TextField textField = new TextField();

    public AdvancedTimeFilter() {
        getChildren().addAll(typeComboButton, textField);
        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalTime>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(textField.getText()));
        });
        //also change predicate every time text in filter was changed
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalTime>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    @Override
    public void clear() {
        textField.setText("");
    }

    public Predicate<LocalTime> getPredicate() {
        return predicateProperty.get();
    }
}
