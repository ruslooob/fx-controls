package com.ruslooob.fxcontrols;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class TableViewBuilder<S> {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy");
    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    private List<ColumnInfo<S, ?>> columnInfos = new ArrayList<>();
    private ObservableList<S> items;

    public static <S> TableViewBuilder<S> builder() {
        return new TableViewBuilder<>();
    }

    public <T> TableViewBuilder<S> addColumn(String colName, ColumnType type, Function<S, Property<T>> propertyGetter) {
        this.columnInfos.add(new ColumnInfo<>(colName, type, propertyGetter));
        return this;
    }

    public TableViewBuilder<S> items(ObservableList<S> items) {
        this.items = items;
        return this;
    }

    public TableView<S> build() {
        Map<String, TextField> filtersByName = new HashMap<>();
        //create columns
        List<TableColumn<S, ?>> columns = new ArrayList<>();
        for (var columnInfo : columnInfos) {
            var columnAndFilter = createColumnAndFilter(columnInfo);
            columns.add(columnAndFilter.first());
            filtersByName.put(columnInfo.columnName(), columnAndFilter.last());
        }
        //build predicate logic for filters
        var filteredData = new FilteredList<>(items, p -> true);
        Map<String, Predicate<S>> predicateMap = new HashMap<>();
        for (var columnInfo : columnInfos) {
            TextField filterTextField = filtersByName.get(columnInfo.columnName());
            // todo каждый раз когда меняется текст, меняется и предикат
            // todo каждый раз, когда меняется и тип фильтра, предикат тоже должен меняться
            filterTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                predicateMap.put(columnInfo.columnName(), createPredicate(columnInfo, newVal));
                filteredData.setPredicate(
                        predicateMap.values().stream().reduce(Predicate::and).orElse(record -> true));
            });
        }

        var tableView = new TableView<S>();
        tableView.getColumns().addAll(columns);
        // build sorting logic
        var sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        return tableView;
    }

    private <T> Pair<TableColumn<S, T>, TextField> createColumnAndFilter(ColumnInfo<S, T> columnInfo) {
        var col = new TableColumn<S, T>();
        col.setPrefWidth(200);
        String colName = columnInfo.columnName();
        col.setId(colName);
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

        var filterTextField = new TextField();
        filterTextField.setId(colName + "-tf");

        var filterTypeComboBox = new ComboBox<String>();
        filterTypeComboBox.getItems().addAll("=", "!=");
        filterTypeComboBox.setValue("=");
        filterTypeComboBox.getStyleClass().add("button-combo-box");

        var colNameWithFilterVBox = new VBox(
                new Label(colName),
                new HBox(5, filterTextField, filterTypeComboBox));
        // todo add combobox with = and !=.
        colNameWithFilterVBox.setPadding(new Insets(5));
        VBox.setVgrow(colNameWithFilterVBox, Priority.ALWAYS);
        HBox.setHgrow(filterTextField, Priority.ALWAYS);
        HBox.setHgrow(filterTypeComboBox, Priority.NEVER);

        col.setGraphic(colNameWithFilterVBox);
        return new Pair<>(col, filterTextField);
    }

    private Predicate<S> createPredicate(ColumnInfo<S, ?> columnInfo, String filterText) {
        if (filterText.isBlank()) {
            return record -> true;
        }
        var propertyGetter = columnInfo.propertyGetter();
        switch (columnInfo.columnType()) {
            case STRING -> {
                return record -> {
                    var cellValue = propertyGetter.apply(record).getValue();
                    if (cellValue == null) {
                        return false;
                    }
                    return cellValue.toString().toLowerCase(Locale.ROOT).contains(filterText.toLowerCase(Locale.ROOT));
                };
            }
            case DATE -> {
                return record -> {
                    var cellValue = propertyGetter.apply(record).getValue();
                    if (cellValue == null) {
                        return false;
                    }
                    LocalDate filterDate;
                    try {
                        filterDate = LocalDate.parse(filterText, dateFormatter);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                    return cellValue.equals(filterDate);
                };
            }
            default -> throw new IllegalArgumentException("Unsupported column type: " + columnInfo.columnType());
        }
    }
}
