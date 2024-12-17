package com.ruslooob.fxcontrols.controls;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedEnumFilter extends AdvancedFilter<String> {

    public AdvancedEnumFilter() {
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
        //do nothing
    }


}
