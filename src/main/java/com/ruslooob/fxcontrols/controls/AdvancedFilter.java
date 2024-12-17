package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.filters.TextFilterType;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

import java.util.List;
import java.util.function.Predicate;

public interface AdvancedFilter<T> {
    void setFilterTypes(List<? extends TextFilterType<T>> filterTypes);
    ObjectProperty<Predicate<T>> predicateProperty();
    void clear();

    //todo This is only for Boolean and Enum. remove this method after refactoring.
    void setTextFilterVisible(boolean visible);

    // todo think about this, i don't like it
    Node getNode();
}
