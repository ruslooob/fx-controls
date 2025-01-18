package com.ruslooob.fxcontrols.model;

import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import javafx.beans.property.Property;
import javafx.scene.control.TableColumn;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.enums.ColumnType.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class ColumnInfo<S, T> {
    TableColumn<S, T> column;
    ColumnType columnType;
    String name;
    Function<S, Property<T>> propertyGetter;
    //some properties which can be used while constructing column filters.
    Map<PropType, Object> props;

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
}
