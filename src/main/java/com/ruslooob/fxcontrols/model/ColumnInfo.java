package com.ruslooob.fxcontrols.model;

import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import javafx.beans.property.Property;
import javafx.scene.control.TableColumn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.enums.ColumnType.*;

public class ColumnInfo<S, T> {
    private final TableColumn<S, T> column;
    private final ColumnType columnType;
    private final String name;
    private final Function<S, Property<T>> propertyGetter;
    //some properties which can be used while constructing column filters.
    private final Map<PropType, Object> props;

    public ColumnInfo(TableColumn<S, T> column, ColumnType columnType) {
        this(column, columnType, Collections.emptyMap());
    }

    public ColumnInfo(TableColumn<S, T> column, ColumnType columnType, Map<PropType, Object> props) {
        this.column = column;
        this.columnType = columnType;
        this.name = column.getText();
        //create property getter function from cellValueFactory
        this.propertyGetter = item -> {
            var features = new TableColumn.CellDataFeatures<S, T>(null, null, item);
            return (Property<T>) column.getCellValueFactory().call(features);
        };
        this.props = props;
    }

    public String name() {
        return name;
    }

    public boolean isSortable() {
        return List.of(STRING, NUMBER, BOOL, DATE, TIME, DATE_TIME).contains(columnType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnInfo<?, ?> that = (ColumnInfo<?, ?>) o;
        return Objects.equals(column, that.column) && columnType == that.columnType && Objects.equals(name, that.name) && Objects.equals(propertyGetter, that.propertyGetter) && Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, columnType, name, propertyGetter, props);
    }

    public TableColumn<S, T> getColumn() {
        return column;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public String getName() {
        return name;
    }

    public Function<S, Property<T>> getPropertyGetter() {
        return propertyGetter;
    }

    public Map<PropType, Object> getProps() {
        return props;
    }
}
