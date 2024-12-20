package com.ruslooob.fxcontrols.model;

import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import javafx.beans.property.Property;
import javafx.scene.control.TableColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class ColumnInfo<S, T> {
    TableColumn<S, T> column;
    ColumnType type;
    String name;
    Function<S, Property<T>> propertyGetter;
    //some properties which can be used while constructing column filters.
    Map<PropType, Object> props;

    public ColumnInfo(TableColumn<S, T> column, ColumnType type) {
        this(column, type, Collections.emptyMap());
    }

    public ColumnInfo(TableColumn<S, T> column, ColumnType type, Map<PropType, Object> props) {
        this.column = column;
        this.type = type;
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
}
