package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterType;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedTextFilter<T> extends HBox implements AdvancedFilter<T> {
    PauseTransition debouncePause = new PauseTransition(Duration.millis(250));
    //todo add clear button
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
            debouncePause.setOnFinished(event -> {
                Function<String, Predicate<T>> searchFunction = changeTypeComboButton.getValue().createSearchFunction();
                predicateProperty.setValue(searchFunction.apply(newVal));
            });
            debouncePause.playFromStart();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setFilterTypes(List<? extends TextFilterType<T>> filterTypes) {
        this.changeTypeComboButton.setItems((List<TextFilterType<T>>) filterTypes);
        this.changeTypeComboButton.setCellConverter(TextFilterType::toString);
        this.changeTypeComboButton.setCellTooltipConverter(TextFilterType::getTooltipText);
    }

    public void setValue(TextFilterType<T> value) {
        this.changeTypeComboButton.setValue(value);
    }

    @Override
    public ObjectProperty<Predicate<T>> predicateProperty() {
        return predicateProperty;
    }

    public Predicate<T> getPredicate() {
        return predicateProperty.get();
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public void setText(String text) {
        this.textField.setText(text);
    }

    @Override
    public void setTextFilterVisible(boolean visible) {
        if (visible) {
            getChildren().setAll(changeTypeComboButton, textField);
            changeTypeComboButton.setPrefWidth(USE_COMPUTED_SIZE);
        } else {
            getChildren().setAll(changeTypeComboButton);
            changeTypeComboButton.setPrefWidth(Integer.MAX_VALUE);
        }
    }

    @Override
    public Node getNode() {
        return this;
    }
}
