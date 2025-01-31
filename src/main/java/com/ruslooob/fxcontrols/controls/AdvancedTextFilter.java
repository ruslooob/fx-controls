package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AdvancedTextFilter<T> extends AdvancedFilter<T> {
    //todo add clear button
    private final TextField textField = new TextField();

    public AdvancedTextFilter(List<? extends TextFilterStrategy<T>> filterTypes) {
        setFilterTypes(filterTypes);
        getChildren().addAll(typeComboButton, textField);
        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<T>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(textField.getText()));
        });
        //also change predicate every time text in filter was changed
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<T>> searchFunction = typeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    @Override
    public void clear() {
        textField.setText("");
    }

    public Predicate<T> getPredicate() {
        return predicateProperty.get();
    }

}
