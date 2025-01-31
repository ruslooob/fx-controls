package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdvancedEnumFilter extends AdvancedFilter<String> {

    public AdvancedEnumFilter(List<? extends TextFilterStrategy<String>> filterTypes) {
        setFilterTypes(filterTypes);

        getChildren().add(typeComboButton);
        typeComboButton.setPrefWidth(Integer.MAX_VALUE);

        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<String>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal.toString()));
        });
    }

    @Override
    public void clear() {
        typeComboButton.resetToDefault();
    }
}
