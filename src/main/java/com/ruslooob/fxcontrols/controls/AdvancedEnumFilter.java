package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedEnumFilter extends AdvancedFilter<String> {
    TextFilterStrategy<String> defaultStrategy;

    public AdvancedEnumFilter(List<? extends TextFilterStrategy<String>> filterTypes) {
        setFilterTypes(filterTypes);
        defaultStrategy = filterTypes.get(0);

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
        typeComboButton.setValue(defaultStrategy);
    }
}
