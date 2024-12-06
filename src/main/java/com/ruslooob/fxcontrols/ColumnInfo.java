package com.ruslooob.fxcontrols;

import javafx.beans.property.Property;

import java.util.function.Function;

public record ColumnInfo<S, T>(String columnName, ColumnType columnType, Function<S, Property<T>> propertyGetter) {
}
