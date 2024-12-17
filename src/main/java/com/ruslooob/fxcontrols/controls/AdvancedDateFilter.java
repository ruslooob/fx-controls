package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterType;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedDateFilter extends HBox implements AdvancedFilter<LocalDate> {
    PauseTransition debouncePause = new PauseTransition(Duration.millis(250));
    // попробовать найти более удобный datepicker, чтобы можно было выбрать год, месяц и день сразу же
    DatePicker datePicker = new DatePicker();
    ComboButton<TextFilterType<LocalDate>> typeComboButton = new ComboButton<>();
    ObjectProperty<Predicate<LocalDate>> predicateProperty = new SimpleObjectProperty<>(s -> true);


    public AdvancedDateFilter() {
        getChildren().addAll(typeComboButton, datePicker);
        datePicker.setEditable(true);
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : dateFormatter.format(date);
            }

            @Override
            public LocalDate fromString(String str) {
                try {
                    return LocalDate.parse(str, dateFormatter);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        //change predicate every time filter type was changed
        typeComboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            Function<String, Predicate<LocalDate>> searchFunction = newVal.createSearchFunction();
            predicateProperty.setValue(searchFunction.apply(datePicker.getEditor().getText()));
        });

        //also change predicate every time text in filter was changed
        datePicker.editorProperty().get().textProperty().addListener((obs, oldVal, newVal) -> {
            debouncePause.setOnFinished(event -> {
                Function<String, Predicate<LocalDate>> searchFunction = typeComboButton.getValue().createSearchFunction();
                predicateProperty.setValue(searchFunction.apply(newVal));
            });
            debouncePause.playFromStart();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setFilterTypes(List<? extends TextFilterType<LocalDate>> filterTypes) {
        this.typeComboButton.setItems((List<TextFilterType<LocalDate>>) filterTypes);
        this.typeComboButton.setCellConverter(TextFilterType::toString);
        this.typeComboButton.setCellTooltipConverter(TextFilterType::getTooltipText);
    }

    @Override
    public ObjectProperty<Predicate<LocalDate>> predicateProperty() {
        return predicateProperty;
    }

    @Override
    public void clear() {
        datePicker.setValue(null);
        datePicker.getEditor().clear();
    }

    @Override
    public void setTextFilterVisible(boolean visible) {
        if (visible) {
            getChildren().setAll(typeComboButton, datePicker);
            typeComboButton.setPrefWidth(USE_COMPUTED_SIZE);
        } else {
            getChildren().setAll(typeComboButton);
            typeComboButton.setPrefWidth(Integer.MAX_VALUE);
        }
    }

    @Override
    public Node getNode() {
        return this;
    }
}
