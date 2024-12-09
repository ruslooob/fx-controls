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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class TableViewBuilder<S> {
    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    private Map<String, ColumnInfo<S, ?>> colInfoByName = new LinkedHashMap<>();
    private ObservableList<S> items;

    public static <S> TableViewBuilder<S> builder() {
        return new TableViewBuilder<>();
    }

    public <T> TableViewBuilder<S> addColumn(TableColumn<S, T> col, ColumnType type) {
        ColumnInfo<S, T> colInfo = new ColumnInfo<>(col, type);
        this.colInfoByName.put(colInfo.name(), colInfo);
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
            var columnAndFilter = createFilterColumn(columnInfo);
            columns.add(columnAndFilter.first());
            filtersByName.put(columnInfo.name(), columnAndFilter.last());
        }
        //build predicate logic for filters
        var filteredData = new FilteredList<>(items, p -> true);
        Map<String, Predicate<?>> predicateMap = new HashMap<>();
        for (var columnInfo : colInfoByName.values()) {
            AdvancedTextFilter<?> filterTextField = filtersByName.get(columnInfo.name());
            filterTextField.predicateProperty().addListener((obs, oldVal, newVal) -> {
                predicateMap.put(columnInfo.name(), filterTextField.getPredicate());
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
            Function<S, ? extends Property<?>> propertyGetter = colInfoByName.get(colName).getPropertyGetter();
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

    private <T> Pair<TableColumn<S, T>, AdvancedTextFilter<?>> createFilterColumn(ColumnInfo<S, T> columnInfo) {
        TableColumn<S, T> col = columnInfo.getColumn();

        AdvancedTextFilter<?> filterTextField = createFilter(columnInfo.getType());
        var colNameWithFilterVBox = new VBox(new Label(columnInfo.name()), filterTextField);
        colNameWithFilterVBox.setPadding(new Insets(5));
        VBox.setVgrow(colNameWithFilterVBox, Priority.ALWAYS);
        col.setText("");
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
