package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedTextFilter<T> extends HBox {
    TextField textField = new TextField();
    ComboButton<TextFilterType<T>> changeTypeComboButton = new ComboButton<>();
    ObjectProperty<Predicate<T>> predicateProperty = new SimpleObjectProperty<>(s -> true);

    public AdvancedTextFilter() {
        getChildren().addAll(changeTypeComboButton, textField);
        //change predicate every time filter type was changed
        changeTypeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<T>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(textField.getText()));
        });
        //also change predicate every time text in filter was changed
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<T>> searchFunction = changeTypeComboButton.getValue().createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(newVal));
        });
    }

    public void setFilterTypes(List<TextFilterType<T>> filterTypes) {
        this.changeTypeComboButton.setItems(filterTypes);
    }

    public void setValue(TextFilterType<T> value) {
        this.changeTypeComboButton.setValue(value);
    }

    public ObjectProperty<Predicate<T>> predicateProperty() {
        return predicateProperty;
    }

    public Predicate<T> getPredicate() {
        return predicateProperty.get();
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public void setTextFilterVisible(boolean visibility) {
        this.textField.setManaged(visibility);
    }
}
