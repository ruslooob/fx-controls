package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterStrategy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AdvancedFilter<T> extends HBox {
    ComboButton<TextFilterStrategy<T>> typeComboButton = new ComboButton<>();
    ObjectProperty<Predicate<T>> predicateProperty = new SimpleObjectProperty<>(s -> true);

    @SuppressWarnings("unchecked")
    public final void setFilterTypes(List<? extends TextFilterStrategy<T>> filterTypes) {
        this.typeComboButton.setItems((List<TextFilterStrategy<T>>) filterTypes);
        this.typeComboButton.setCellConverter(TextFilterStrategy::toString);
        this.typeComboButton.setCellTooltipConverter(TextFilterStrategy::getTooltipText);
    }

    public final ObjectProperty<Predicate<T>> predicateProperty() {
        return predicateProperty;
    }

    public void clear() {
        //do nothing
    }
}
