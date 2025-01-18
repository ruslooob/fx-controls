package com.ruslooob.fxcontrols.controls;


import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import com.ruslooob.fxcontrols.filters.TextFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.AllIncludeEnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.EnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterStrategy;
import com.ruslooob.fxcontrols.filters.time.TimeAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.time.TimeBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.time.TimeEqualsFilterStrategy;
import com.ruslooob.fxcontrols.model.ColumnInfo;
import javafx.animation.PauseTransition;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ruslooob.fxcontrols.enums.PropType.BOOLEAN_TYPES;
import static com.ruslooob.fxcontrols.enums.PropType.ENUM_FILTER_TYPES;
import static javafx.collections.FXCollections.observableArrayList;

@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdvancedTableView<S> extends TableView<S> {
    static final String DEFAULT_BOOL_TRUE_STR = "ДА";
    static final String DEFAULT_BOOL_FALSE_STR = "Нет";

    final ObservableList<S> items = observableArrayList();
    final ObservableList<S> sortedItems = observableArrayList();
    final FilteredList<S> filteredItems;
    @Getter
    final ObservableList<S> selectedItems = observableArrayList();

    boolean enableRowNumCol = false;
    boolean enableMultiSelect = false;
    // Some basic info about columns
    final List<ColumnInfo<S, ?>> colsInfo = new ArrayList<>();//todo add sort type enum that was apply after checking sort buttons in table header
    // Contains all predicate filters in columns
    //fixme varialbe can be local
    final Map<ColumnInfo<S, ?>, Predicate<?>> predicateMap = new HashMap<>();
    @Setter
    Consumer<List<S>> exportToCsvHandler;

    static final FontIcon sortUpIcon = new FontIcon(FontAwesomeSolid.SORT_UP);
    static final FontIcon sortDownIcon = new FontIcon(FontAwesomeSolid.SORT_DOWN);

    final PauseTransition debouncePause = new PauseTransition(Duration.millis(250));

    public AdvancedTableView() {
        filteredItems = new FilteredList<>(sortedItems, p -> true);
        setItems(filteredItems);

        getColumns().add(0, createActionsColumn());

        sortUpIcon.setIconSize(21);
        sortDownIcon.setIconSize(21);
    }

    public void setData(ObservableList<S> items) {
        this.items.setAll(items);
        this.sortedItems.setAll(observableArrayList(items));
    }

    public void clearData(ObservableList<S> items) {
        this.items.clear();
    }

    private TableColumn<S, Integer> createActionsColumn() {
        var col = new TableColumn<S, Integer>();
        col.setPrefWidth(50);

        var burgerIcon = new FontIcon(FontAwesomeSolid.BARS);

        var menuButton = new MenuButton();
        menuButton.setGraphic(burgerIcon);
        menuButton.getItems().addAll(
                createClearFiltersMenuItem(),
                createChangeTableColumnsMenuItem(),
                createExportToCsvMenuItem());

        col.setGraphic(menuButton);
        col.setSortable(false);
        return col;
    }

    private MenuItem createClearFiltersMenuItem() {
        List<TableColumn<S, ?>> columns = getColumns();

        var menuItem = new MenuItem("Сбросить фильтры");
        menuItem.setOnAction(event -> {
            for (int i = 1; i < columns.size(); i++) {
                if (i == 1 && enableMultiSelect) {
                    continue;
                }
                TableColumn<S, ?> column = columns.get(i);
                // todo fix this triple cast. May be rewrite this as class
                AdvancedFilter<?> filter = (AdvancedFilter<?>) ((HBox) ((VBox) column.getGraphic()).getChildren().get(1)).getChildren().get(0);
                filter.clear();
            }
        });
        return menuItem;
    }

    private MenuItem createChangeTableColumnsMenuItem() {
        List<TableColumn<S, ?>> columns = getColumns();

        var menuItem = new MenuItem("Настройка колонок");
        menuItem.setOnAction(event -> {
            var colSettingsGrid = new GridPane();
            colSettingsGrid.setHgap(10);
            colSettingsGrid.setVgap(5);

            var col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
            var col2 = new ColumnConstraints();
            col2.setHgrow(Priority.SOMETIMES);
            colSettingsGrid.getColumnConstraints().addAll(col1, col2);

            int row = 0;
            for (int i = 1; i < columns.size(); i++) {
                TableColumn<S, ?> col = columns.get(i);
                if (i == 1 && enableMultiSelect) {
                    continue;
                }
                String colName = ((Label) col.getGraphic().lookup(".label")).getText();

                var toggleSwitch = new ToggleSwitch();
                toggleSwitch.setSelected(col.isVisible());
                toggleSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    col.setVisible(newVal);
                });

                var colNameLabel = new Label(colName);

                colSettingsGrid.add(colNameLabel, 0, row);
                colSettingsGrid.add(toggleSwitch, 1, row);
                row++;
            }
            colSettingsGrid.setPadding(new Insets(20));
            // todo style this stage properly, only one close button and white background
            var stage = new Stage();
            stage.initOwner(this.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            var scene = new Scene(colSettingsGrid, 300, 200);
            stage.setScene(scene);
            stage.show();
        });

        return menuItem;
    }

    private MenuItem createExportToCsvMenuItem() {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        var menuItem = new MenuItem("Export to CSV");
        menuItem.setOnAction(exportToCsvHandler == null ? event -> {
            File saveFile = fileChooser.showSaveDialog(this.getScene().getWindow());
            if (saveFile != null) {
                writeToCsv(saveFile, filteredItems);
            }
        } : event -> exportToCsvHandler.accept(filteredItems));
        return menuItem;
    }

    private void writeToCsv(File destFile, List<S> sortedData) {
        var stringBuilder = new StringBuilder();
        List<List<Object>> dataForExport = getDataForExport(sortedData);
        for (List<Object> rowData : dataForExport) {
            for (Object cellVal : rowData) {
                stringBuilder.append(cellVal.toString()).append(";");
            }
            stringBuilder.append(System.lineSeparator());
        }

        try {
            Files.writeString(destFile.toPath(), stringBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> getDataForExport(List<S> sortedData) {
        List<TableColumn<S, ?>> columns = getColumns();
        List<List<Object>> result = new ArrayList<>();

        //add first row for csv output
        result.add(colsInfo.stream().map(i -> (Object) i.getName()).toList());

        for (S item : sortedData) {
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                //skip unnecessary columns
                if (i == 0 || (enableMultiSelect && i == 1)) {
                    continue;
                }
                row.add(columns.get(i).getCellData(item));
            }
            result.add(row);
        }
        return result;
    }

    public void addColumn(TableColumn<S, ?> col, ColumnType type) {
        addColumn(col, type, Collections.emptyMap());
    }

    public void addColumn(TableColumn<S, ?> col, ColumnType type, Map<PropType, Object> props) {
        ColumnInfo<S, ?> columnInfo = new ColumnInfo<>(col, type, props);
        this.colsInfo.add(columnInfo);

        col.setText("");
        //remove unnecessary default behavior with sorting
        col.setSortable(false);
        col.setSortType(null);

        var filterColumnName = new Label(columnInfo.name());
        filterColumnName.setFont(new Font(filterColumnName.getFont().getName(), filterColumnName.getFont().getSize() + 3));

        AdvancedFilter<?> columnFilter = createColumnFilter(columnInfo.getColumnType(), columnInfo.getProps());
        columnFilter.predicateProperty().addListener((obs, oldVal, newVal) -> {
            debouncePause.setOnFinished(event -> {
                predicateMap.put(columnInfo, newVal);
                filteredItems.setPredicate(combinePredicates());
            });
            debouncePause.playFromStart();
        });

        var nameWithFilterVbox = new VBox(5);
        nameWithFilterVbox.setAlignment(Pos.CENTER);
        nameWithFilterVbox.setPadding(new Insets(5));

        var filterWithSort = new HBox(5, columnFilter);
        nameWithFilterVbox.getChildren().addAll(filterColumnName, filterWithSort);
        col.setGraphic(nameWithFilterVbox);
        filterWithSort.setAlignment(Pos.CENTER);
        if (columnInfo.isSortable()) {
            Function<S, ? extends Property<?>> propertyGetter = columnInfo.getPropertyGetter();
            Comparator<S> ascComparator = (r1, r2) -> {
                Comparable value1 = (Comparable<?>) propertyGetter.apply(r1).getValue();
                Comparable value2 = (Comparable<?>) propertyGetter.apply(r2).getValue();
                return value1.compareTo(value2);
            };
            Comparator<S> descComparator = (r1, r2) -> {
                Comparable value1 = (Comparable<?>) propertyGetter.apply(r1).getValue();
                Comparable value2 = (Comparable<?>) propertyGetter.apply(r2).getValue();
                return value2.compareTo(value1);
            };

            col.sortTypeProperty().addListener((obs, oldSortType, newSortType) -> {
                if (newSortType == TableColumn.SortType.ASCENDING) {
                    sortedItems.sort(ascComparator);
                    filterWithSort.getChildren().add(sortUpIcon);
                } else if (newSortType == TableColumn.SortType.DESCENDING) {
                    sortedItems.sort(descComparator);
                    filterWithSort.getChildren().remove(1);
                    filterWithSort.getChildren().add(sortDownIcon);
                } else {
                    sortedItems.setAll(items);
                    filterWithSort.getChildren().remove(1);
                }
            });

            col.getGraphic().addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                colsInfo.forEach(info -> {
                    //reset other columns
                    if (!info.getColumn().equals(col)) {
                        info.getColumn().setSortType(null);
                    }
                });
                //toggle sorting
                if (col.getSortType() == null) {
                    col.setSortType(TableColumn.SortType.ASCENDING);
                } else if (col.getSortType() == TableColumn.SortType.ASCENDING) {
                    col.setSortType(TableColumn.SortType.DESCENDING);
                } else {
                    col.setSortType(null);
                }
            });
        }
        getColumns().add(col);
    }

    private AdvancedFilter<?> createColumnFilter(ColumnType type, Map<PropType, Object> colProps) {
        switch (type) {
            case STRING -> {
                return new AdvancedTextFilter<>(List.of(new SubstringFilterStrategy(), new EqualsFilterTypeStrategy(), new StartsWithFilterTypeStrategy()));
            }
            case NUMBER -> {
                return new AdvancedTextFilter<>(List.of(new NumberEqualsFilterStrategy(), new NumberBeforeFilterStrategy(), new NumberAfterFilterStrategy()));
            }
            case BOOL -> {
                List<String> filterTypes = (List<String>) colProps.get(BOOLEAN_TYPES);
                List<TextFilterStrategy<String>> enumFilterTypes;
                if (filterTypes != null) {
                    enumFilterTypes = filterTypes.stream().map(EnumFilterStrategy::new).map(f -> (TextFilterStrategy<String>) f).collect(Collectors.toList());
                    enumFilterTypes.add(0, new AllIncludeEnumFilterStrategy());
                } else {
                    enumFilterTypes = List.of(new AllIncludeEnumFilterStrategy(), new EnumFilterStrategy("Да"), new EnumFilterStrategy("Нет"));
                }

                return new AdvancedEnumFilter(enumFilterTypes);
            }
            case DATE -> {
                // todo Think about implementing more intelligent filters for search by day, month, and year and some patterns.
                // This could impact the search speed, making it slower. Maybe implement some trade-off solution, for example DatePatternStrategy
                return new AdvancedDateFilter(List.of(new DateEqualsFilterStrategy(), new DateBeforeFilterStrategy(), new DateAfterFilterStrategy()));
            }
            case TIME -> {
                return new AdvancedTimeFilter(List.of(new TimeEqualsFilterStrategy(), new TimeBeforeFilterStrategy(), new TimeAfterFilterStrategy()));
            }
            case ENUM -> {
                List<String> filterTypes = (List<String>) colProps.get(ENUM_FILTER_TYPES);
                if (filterTypes == null) {
                    throw new IllegalArgumentException("Cannot create enum column filter without prop: %s. Please, pass it via addColumn method.".formatted(ENUM_FILTER_TYPES));
                }
                List<TextFilterStrategy<String>> enumFilterTypes = filterTypes.stream().map(EnumFilterStrategy::new).map(f -> (TextFilterStrategy<String>) f).collect(Collectors.toList());
                enumFilterTypes.add(0, new AllIncludeEnumFilterStrategy());
                return new AdvancedEnumFilter(enumFilterTypes);
            }
            default -> throw new IllegalArgumentException("Unknown column type %s".formatted(type));
        }
    }

    /**
     * Создает один предикат на основе всех фильтров-предикатов в колонках таблицы
     */
    private Predicate<S> combinePredicates() {
        Predicate<S> resPredicate = p -> true;
        for (Map.Entry<ColumnInfo<S, ?>, Predicate<?>> predicateEntry : predicateMap.entrySet()) {
            ColumnInfo<S, ?> colInfo = predicateEntry.getKey();
            Predicate<?> filterPredicate = predicateEntry.getValue();
            Function<S, ? extends Property<?>> propertyGetter = colInfo.getPropertyGetter();
            // convert table column predicate to generic Predicate<S>
            Predicate<S> predicate = record -> {
                Property<?> property = propertyGetter.apply(record);
                if (property == null) {
                    return false;
                }
                Object value = property.getValue();
                if (colInfo.getColumnType() != ColumnType.BOOL) {
                    return ((Predicate<Object>) filterPredicate).test(value);
                } else {
                    // т.к ColumnType.BOOL реализован через ENUM, то нам нужно преобразовать boolean в String
                    return ((Predicate<Object>) filterPredicate).test(mapBoolToString((boolean) value, (List<String>) colInfo.getProps().get(BOOLEAN_TYPES)));
                }
            };
            resPredicate = resPredicate.and(predicate);
        }

        return resPredicate;
    }

    private String mapBoolToString(boolean val, List<String> filterTypes) {
        if (filterTypes == null) {
            return val ? DEFAULT_BOOL_TRUE_STR : DEFAULT_BOOL_FALSE_STR;
        } else {
            if (filterTypes.size() != 2) {
                throw new IllegalArgumentException("Incorrect size of %s: it must be 2, given %s".formatted(BOOLEAN_TYPES, filterTypes));
            }
            return val ? filterTypes.get(0) : filterTypes.get(1);
        }
    }

    public void enableMultiSelect(boolean enable) {
        this.enableMultiSelect = enable;
        if (enable) {
            getColumns().add(1, createCheckBoxColumn("Выбрать", selectedItems));
        }
    }

    private TableColumn<S, Boolean> createCheckBoxColumn(String columnName, ObservableList<S> selectedItems) {
        TableColumn<S, Boolean> checkBoxColumn = new TableColumn<>(columnName);
        //todo add check all column
        checkBoxColumn.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    S rowItem = getTableRow().getItem();
                    if (rowItem != null) {
                        if (checkBox.isSelected()) {
                            selectedItems.add(rowItem);
                        } else {
                            //todo possible performance issues on large table.
                            // If this true, keep selected items in sorted order and remove via binary search.
                            selectedItems.remove(rowItem);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                    S rowItem = getTableRow().getItem();
                    checkBox.setSelected(selectedItems.contains(rowItem));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        checkBoxColumn.setSortable(false);
        return checkBoxColumn;
    }

    public void enableRowNumColumn(boolean enable) {
        this.enableRowNumCol = enable;
        if (enable) {
            //reuse empty action column for specifying rowNum
            TableColumn<S, Integer> actionCol = (TableColumn<S, Integer>) getColumns().get(0);
            actionCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null) {
                        setText(null);
                    } else {
                        setText(String.valueOf(getTableRow().getIndex() + 1));
                    }
                }
            });
        }
    }


}
