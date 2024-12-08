package com.ruslooob.fxcontrols;

import com.ruslooob.fxcontrols.controls.AdvancedTextFilter;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilter;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterType;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterType;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterType;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

@SuppressWarnings("unchecked")
public class TableViewBuilder<S> {
    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    private Map<String, ColumnInfo<S, ?>> colInfoByName = new LinkedHashMap<>();
    private ObservableList<S> items;

    public static <S> TableViewBuilder<S> builder() {
        return new TableViewBuilder<>();
    }

    public <T> TableViewBuilder<S> addColumn(String colName, ColumnType type, Function<S, Property<T>> propertyGetter) {
        this.colInfoByName.put(colName, new ColumnInfo<>(colName, type, propertyGetter));
        return this;
    }

    public TableViewBuilder<S> items(ObservableList<S> items) {
        this.items = items;
        return this;
    }

    public TableView<S> build() {
        Map<String, AdvancedTextFilter<?>> filtersByName = new HashMap<>();
        //create columns
        List<TableColumn<S, ?>> columns = new ArrayList<>();
        for (var columnInfo : colInfoByName.values()) {
            var columnAndFilter = createColumnAndFilter(columnInfo);
            columns.add(columnAndFilter.first());
            filtersByName.put(columnInfo.columnName(), columnAndFilter.last());
        }
        //build predicate logic for filters
        var filteredData = new FilteredList<>(items, p -> true);
        Map<String, Predicate<?>> predicateMap = new HashMap<>();
        for (var columnInfo : colInfoByName.values()) {
            AdvancedTextFilter<?> filterTextField = filtersByName.get(columnInfo.columnName());
            filterTextField.predicateProperty().addListener((obs, oldVal, newVal) -> {
                predicateMap.put(columnInfo.columnName(), filterTextField.getPredicate());
                filteredData.setPredicate(combinePredicates(predicateMap));
            });
        }

        var tableView = new TableView<S>();
        tableView.getColumns().addAll(columns);
        //build sorting logic
        var sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        return tableView;
    }

    /**
     * Создает один предикат на основе всех фильтров-предикатов в колонках таблицы
     */
    private Predicate<S> combinePredicates(Map<String, Predicate<?>> predicateMap) {
        Predicate<S> resPredicate = p -> true;

        for (Map.Entry<String, Predicate<?>> predicateEntry : predicateMap.entrySet()) {
            String colName = predicateEntry.getKey();
            Predicate<?> filterPredicate = predicateEntry.getValue();
            Function<S, ? extends Property<?>> propertyGetter = colInfoByName.get(colName).propertyGetter();
            Predicate<S> predicate = record -> {
                Property<?> property = propertyGetter.apply(record);
                if (property == null) {
                    return false;
                }
                Object value = property.getValue();
                return ((Predicate<Object>) filterPredicate).test(value);
            };
            resPredicate = resPredicate.and(predicate);
        }

        return resPredicate;
    }

    private <T> Pair<TableColumn<S, T>, AdvancedTextFilter<?>> createColumnAndFilter(ColumnInfo<S, T> columnInfo) {
        var col = new TableColumn<S, T>();
        col.setPrefWidth(200);
        String colName = columnInfo.columnName();
        col.setCellValueFactory(cellData -> columnInfo.propertyGetter().apply(cellData.getValue()));

        if (columnInfo.columnType() == ColumnType.DATE) {
            col.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
                @Override
                public String toString(T date) {
                    return date != null ? ((LocalDate) date).format(dateFormatter) : "";
                }

                @Override
                public T fromString(String string) {
                    return string != null && !string.isEmpty() ? (T) LocalDate.parse(string, dateFormatter) : null;
                }
            }));
        }

        AdvancedTextFilter<?> filterTextField = createFilter(columnInfo.columnType());
        var colNameWithFilterVBox = new VBox(new Label(colName), filterTextField);
        colNameWithFilterVBox.setPadding(new Insets(5));
        VBox.setVgrow(colNameWithFilterVBox, Priority.ALWAYS);
        HBox.setHgrow(filterTextField, Priority.ALWAYS);

        col.setGraphic(colNameWithFilterVBox);
        return new Pair<>(col, filterTextField);
    }

    private static AdvancedTextFilter<?> createFilter(ColumnType type) {
        switch (type) {
            case STRING -> {
                var filterTextField = new AdvancedTextFilter<String>();
                filterTextField.setFilterTypes(List.of(new SubstringFilterType(), new EqualsFilterType(), new StartsWithFilterType()));
                return filterTextField;
            }
            case DATE -> {
                var filterTextField = new AdvancedTextFilter<LocalDate>();
                filterTextField.setFilterTypes(List.of(new DateEqualsFilter()));
                return filterTextField;
            }
            default -> throw new IllegalArgumentException("Unknown column type %s".formatted(type));
        }
    }

}
