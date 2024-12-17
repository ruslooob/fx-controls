package com.ruslooob.fxcontrols.controls;

import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedTextFilter<T> extends AdvancedFilter<T> {
    PauseTransition debouncePause = new PauseTransition(Duration.millis(250));
    //todo add clear button
    TextField textField = new TextField();

    public AdvancedTextFilter() {
        getChildren().addAll(typeComboButton, textField);
        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<T>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(textField.getText()));
        });
        //also change predicate every time text in filter was changed
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            debouncePause.setOnFinished(event -> {
                Function<String, Predicate<T>> searchFunction = typeComboButton.getValue().createSearchFunction();
                predicateProperty.setValue(searchFunction.apply(newVal));
            });
            debouncePause.playFromStart();
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
