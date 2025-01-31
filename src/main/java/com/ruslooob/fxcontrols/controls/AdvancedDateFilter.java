package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdvancedDateFilter extends AdvancedFilter<LocalDate> {
    private final MaskedTextField dateTextField = new MaskedTextField("##.##.####");

    public AdvancedDateFilter(List<? extends TextFilterStrategy<LocalDate>> filterTypes) {
        setFilterTypes(filterTypes);
        getChildren().addAll(typeComboButton, dateTextField);

        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalDate>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(dateTextField.getText()));
        });

        //also change predicate every time text in filter was changed
        dateTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            // check that user input full time before creating search function
            if (dateTextField.getPlainText().length() < 8) {
                predicateProperty.setValue(o -> true);
                return;
            }
            Function<String, Predicate<LocalDate>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    @Override
    public void clear() {
        this.dateTextField.setText("");
    }
}
