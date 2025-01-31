package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdvancedTimeFilter extends AdvancedFilter<LocalTime> {
    private final MaskedTextField dateTimeTextField = new MaskedTextField("##:##");

    public AdvancedTimeFilter(List<? extends TextFilterStrategy<LocalTime>> filterTypes) {
        setFilterTypes(filterTypes);
        getChildren().addAll(typeComboButton, dateTimeTextField);
        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalTime>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(dateTimeTextField.getText()));
        });
        //also change predicate every time text in filter was changed
        dateTimeTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            // check that user input full date before creating search function
            if (dateTimeTextField.getPlainText().length() < 4) {
                predicateProperty.setValue(o -> true);
                return;
            }
            Function<String, Predicate<LocalTime>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    @Override
    public void clear() {
        dateTimeTextField.setPlainText("");
    }

    public Predicate<LocalTime> getPredicate() {
        return predicateProperty.get();
    }
}
