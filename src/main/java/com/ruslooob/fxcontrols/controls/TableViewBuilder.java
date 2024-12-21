package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import com.ruslooob.fxcontrols.filters.TextFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.datetime.TimeEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.AllIncludeEnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.enumeration.EnumFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberAfterFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberBeforeFilterStrategy;
import com.ruslooob.fxcontrols.filters.number.NumberEqualsFilterStrategy;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterTypeStrategy;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterStrategy;
import com.ruslooob.fxcontrols.model.ColumnInfo;
import javafx.animation.PauseTransition;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ruslooob.fxcontrols.enums.PropType.BOOLEAN_TYPES;
import static com.ruslooob.fxcontrols.enums.PropType.ENUM_FILTER_TYPES;

@SuppressWarnings("unchecked")
//todo add clear all filters button
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableViewBuilder<S> {
    static final String DEFAULT_BOOL_TRUE_STR = "ДА";
    static final String DEFAULT_BOOL_FALSE_STR = "Нет";

    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    final List<ColumnInfo<S, ?>> colInfoList = new ArrayList<>();
    boolean enableRowNumCol = false;
    ObservableList<S> items;
    SortedList<S> sortedData;

    final PauseTransition debouncePause = new PauseTransition(Duration.millis(250));

    private Consumer<List<S>> exportToCsvHandler;

    public static <S> TableViewBuilder<S> builder() {
        return new TableViewBuilder<>();
    }

    public <T> TableViewBuilder<S> addColumn(TableColumn<S, T> col, ColumnType type) {
        return addColumn(col, type, Collections.emptyMap());
    }

    public <T> TableViewBuilder<S> addColumn(TableColumn<S, T> col, ColumnType type, Map<PropType, Object> props) {
        ColumnInfo<S, T> colInfo = new ColumnInfo<>(col, type, props);
        this.colInfoList.add(colInfo);
        return this;
    }

    public TableViewBuilder<S> items(ObservableList<S> items) {
        this.items = items;
        return this;
    }

    public TableViewBuilder<S> addRowNumColumn() {
        this.enableRowNumCol = true;
        return this;
    }

    public TableViewBuilder<S> exportToCsvHandler(Consumer<List<S>> exportToCsvHandler) {
        this.exportToCsvHandler = exportToCsvHandler;
        return this;
    }

    public AdvancedTableView<S> build() {
        var filteredData = new FilteredList<>(items, p -> true);
        Map<ColumnInfo<S, ?>, Predicate<?>> predicateMap = new HashMap<>();

        List<TableColumn<S, ?>> columns = new ArrayList<>();
        for (var columnInfo : colInfoList) {
            TableColumn<S, ?> col = columnInfo.getColumn();
            col.setText("");
            Label filterColumnName = new Label(columnInfo.name());
            filterColumnName.setFont(new Font(filterColumnName.getFont().getName(), filterColumnName.getFont().getSize() + 3));

            VBox nameWithFilterVbox = new VBox(5);
            var columnFilter = createColumnFilter(columnInfo.getType(), columnInfo.getProps());
            columnFilter.predicateProperty().addListener((obs, oldVal, newVal) -> {
                debouncePause.setOnFinished(event -> {
                    predicateMap.put(columnInfo, newVal);
                    filteredData.setPredicate(combinePredicates(predicateMap));
                });
                debouncePause.playFromStart();
            });

            nameWithFilterVbox.setAlignment(Pos.CENTER);
            nameWithFilterVbox.setPadding(new Insets(5));
            nameWithFilterVbox.getChildren().addAll(filterColumnName, columnFilter);
            col.setGraphic(nameWithFilterVbox);
            columns.add(col);
        }

        var tableView = new AdvancedTableView<S>();
        // todo refactor deep columns pass
        TableColumn<S, Integer> actionCol = createActionsColumn(tableView, columns);
        columns.add(0, actionCol);
        //todo move this code for creating filters inside AdvancedTableView
        tableView.setMultiSelect(true);

        if (enableRowNumCol) {
            //reuse empty action column for specifying rowNum
            actionCol.setCellValueFactory(cellData -> {
                int index = tableView.getItems().indexOf(cellData.getValue()) + 1;
                return new SimpleObjectProperty<>(index);
            });
        }
        tableView.getColumns().addAll(columns);

        //build sorting logic
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        return tableView;
    }

    private AdvancedFilter<?> createColumnFilter(ColumnType type, Map<PropType, Object> colProps) {
        switch (type) {
            case STRING -> {
                // todo think about constructor pass List<FilterTypes> after creating AdvancedEnumControl
                var filter = new AdvancedTextFilter<String>();
                filter.setFilterTypes(List.of(new SubstringFilterStrategy(), new EqualsFilterTypeStrategy(), new StartsWithFilterTypeStrategy()));
                return filter;
            }
            case NUMBER -> {
                var filter = new AdvancedTextFilter<Number>();
                filter.setFilterTypes(List.of(new NumberEqualsFilterStrategy(), new NumberBeforeFilterStrategy(), new NumberAfterFilterStrategy()));
                return filter;
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

                var filter = new AdvancedEnumFilter();
                filter.setFilterTypes(enumFilterTypes);
                return filter;
            }
            case DATE -> {
                // todo Think about implementing more intelligent filters for search by day, month, and year and some patterns.
                // This could impact the search speed, making it slower. Maybe implement some trade-off solution, for example DatePatternStrategy
                var filter = new AdvancedDateFilter();
                filter.setFilterTypes(List.of(new DateEqualsFilterStrategy(), new DateBeforeFilterStrategy(), new DateAfterFilterStrategy()));
                return filter;
            }
            case TIME -> {
                var filter = new AdvancedTimeFilter();
                //todo implement later
                filter.setFilterTypes(List.of(new TimeEqualsFilterStrategy()));
                return filter;
            }
            case ENUM -> {
                List<String> filterTypes = (List<String>) colProps.get(ENUM_FILTER_TYPES);
                if (filterTypes == null) {
                    throw new IllegalArgumentException("Cannot create enum column filter without prop: %s. Please, pass it via addColumn method.".formatted(ENUM_FILTER_TYPES));
                }
                var filter = new AdvancedEnumFilter();
                List<TextFilterStrategy<String>> enumFilterTypes = filterTypes.stream().map(EnumFilterStrategy::new).map(f -> (TextFilterStrategy<String>) f).collect(Collectors.toList());
                enumFilterTypes.add(0, new AllIncludeEnumFilterStrategy());
                filter.setFilterTypes(enumFilterTypes);
                return filter;
            }
            default -> throw new IllegalArgumentException("Unknown column type %s".formatted(type));
        }
    }

    /**
     * Создает один предикат на основе всех фильтров-предикатов в колонках таблицы
     */
    private Predicate<S> combinePredicates(Map<ColumnInfo<S, ?>, Predicate<?>> predicateMap) {
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
                if (colInfo.getType() != ColumnType.BOOL) {
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

    private TableColumn<S, Integer> createActionsColumn(Parent background, List<TableColumn<S, ?>> columns) {
        var col = new TableColumn<S, Integer>();
        col.setPrefWidth(50);

        var burgerIcon = new FontIcon(FontAwesomeSolid.BARS);

        var menuButton = new MenuButton();
        menuButton.setGraphic(burgerIcon);
        menuButton.getItems().addAll(createClearFiltersMenuItem(columns),
                createChangeTableColumnsMenuItem(background, columns),
                createExportToCsvMenuItem(background, columns));

        col.setGraphic(menuButton);
        col.setSortable(false);
        return col;
    }

    private MenuItem createClearFiltersMenuItem(List<TableColumn<S, ?>> columns) {
        var menuItem = new MenuItem("Сбросить фильтры");
        menuItem.setOnAction(event -> {
            for (int i = 1; i < columns.size(); i++) {
                TableColumn<S, ?> column = columns.get(i);
                // todo fix this double cast
                AdvancedFilter<?> filter = (AdvancedFilter<?>) ((VBox) column.getGraphic()).getChildren().get(1);
                filter.clear();
            }
        });
        return menuItem;
    }

    private MenuItem createChangeTableColumnsMenuItem(Parent background, List<TableColumn<S, ?>> columns) {
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
            stage.initOwner(background.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            var scene = new Scene(colSettingsGrid, 300, 200);
            stage.setScene(scene);
            stage.show();
        });

        return menuItem;
    }

    private MenuItem createExportToCsvMenuItem(Parent background, List<TableColumn<S, ?>> columns) {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        var menuItem = new MenuItem("Export to CSV");
        menuItem.setOnAction(exportToCsvHandler == null ? event -> {
            File saveFile = fileChooser.showSaveDialog(background.getScene().getWindow());
            if (saveFile != null) {
                writeToCsv(saveFile, sortedData, columns);
            }
        } : event -> exportToCsvHandler.accept(sortedData));
        return menuItem;
    }

    private void writeToCsv(File destFile, List<S> sortedData, List<TableColumn<S, ?>> columns) {
        var stringBuilder = new StringBuilder();
        List<List<Object>> dataForExport = getDataForExport(sortedData, columns);
        for (List<Object> rowData : dataForExport) {
            for (Object cellVal : rowData) {
                stringBuilder.append(cellVal.toString()).append(";");
            }
            stringBuilder.append(System.lineSeparator());
        }
        try {
            Files.write(destFile.toPath(), stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> getDataForExport(List<S> sortedData, List<TableColumn<S, ?>> columns) {
        List<List<Object>> result = new ArrayList<>();

        //add first row for csv output
        result.add(colInfoList.stream().map(i -> (Object) i.getName()).toList());

        for (S item : sortedData) {
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                //skip first from csv export
                if (i == 0 && enableRowNumCol) {
                    continue;
                }
                row.add(columns.get(i).getCellData(item));
            }
            result.add(row);
        }
        return result;
    }
}