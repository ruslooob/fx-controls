package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdvancedDateTimeFilter extends AdvancedFilter<LocalDateTime> {
    private final MaskedTextField dateTimeTextField = new MaskedTextField("##.##.#### ##:##");

    public AdvancedDateTimeFilter(List<? extends TextFilterStrategy<LocalDateTime>> filterTypes) {
        setFilterTypes(filterTypes);
        getChildren().addAll(typeComboButton, dateTimeTextField);

        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalDateTime>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(dateTimeTextField.getText()));
        });

        //also change predicate every time text in filter was changed
        dateTimeTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            // check that user input full time before creating search function
            if (dateTimeTextField.getPlainText().length() < 12) {
                predicateProperty.setValue(o -> true);
                return;
            }
            Function<String, Predicate<LocalDateTime>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    @Override
    public void clear() {
        this.dateTimeTextField.setPlainText("");
    }
}
