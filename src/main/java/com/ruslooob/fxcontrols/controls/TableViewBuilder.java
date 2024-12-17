package com.ruslooob.fxcontrols.controls;

import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.enums.PropType;
import com.ruslooob.fxcontrols.filters.TextFilterType;
import com.ruslooob.fxcontrols.filters.date.DateAfterFilter;
import com.ruslooob.fxcontrols.filters.date.DateBeforeFilter;
import com.ruslooob.fxcontrols.filters.date.DateEqualsFilter;
import com.ruslooob.fxcontrols.filters.enumeration.AllIncludeEnumFilter;
import com.ruslooob.fxcontrols.filters.enumeration.EnumFilter;
import com.ruslooob.fxcontrols.filters.number.NumberAfterFilter;
import com.ruslooob.fxcontrols.filters.number.NumberBeforeFilter;
import com.ruslooob.fxcontrols.filters.number.NumberEqualsFilter;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterType;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterType;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterType;
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
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
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
public class TableViewBuilder<S> {
    private static final String DEFAULT_BOOL_TRUE_STR = "ДА";
    private static final String DEFAULT_BOOL_FALSE_STR = "Нет";

    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    private Map<String, ColumnInfo<S, ?>> colInfoByName = new LinkedHashMap<>();
    private boolean enableRowNumCol = false;
    private ObservableList<S> items;
    private SortedList<S> sortedData;
    //some properties which can be used while constructing column filters. Map<colName, Map<PropType, Object>>
    private Map<String, Map<PropType, Object>> props = new HashMap<>();

    private final PauseTransition debouncePause = new PauseTransition(Duration.millis(250));

    @Setter
    private Consumer<List<S>> exportToCsvHandler;

    public static <S> TableViewBuilder<S> builder() {
        return new TableViewBuilder<>();
    }

    public <T> TableViewBuilder<S> addColumn(TableColumn<S, T> col, ColumnType type) {
        ColumnInfo<S, T> colInfo = new ColumnInfo<>(col, type);
        this.colInfoByName.put(colInfo.name(), colInfo);
        return this;
    }

    public <T> TableViewBuilder<S> addColumn(TableColumn<S, T> col, ColumnType type, Map<PropType, Object> props) {
        this.props.put(col.getText(), props);
        return addColumn(col, type);
    }

    public TableViewBuilder<S> items(ObservableList<S> items) {
        this.items = items;
        return this;
    }

    public TableViewBuilder<S> addRowNumColumn() {
        this.enableRowNumCol = true;
        return this;
    }

    public TableView<S> build() {
        Map<String, AdvancedFilter<?>> filtersByName = new HashMap<>();
        //create columns
        List<TableColumn<S, ?>> columns = new ArrayList<>();
        for (var columnInfo : colInfoByName.values()) {
            TableColumn<S, ?> col = columnInfo.getColumn();
            col.setText("");
            Label filterColumnName = new Label(columnInfo.name());
            filterColumnName.setFont(new Font(filterColumnName.getFont().getName(), filterColumnName.getFont().getSize() + 3));

            VBox nameWithFilterVbox = new VBox(5);
            var columnFilter = createColumnFilter(columnInfo.getType(), props.getOrDefault(columnInfo.getName(), new HashMap<>()));
            nameWithFilterVbox.setAlignment(Pos.CENTER);
            nameWithFilterVbox.setPadding(new Insets(5));
            nameWithFilterVbox.getChildren().addAll(filterColumnName, columnFilter);

            col.setGraphic(nameWithFilterVbox);
            columns.add(col);

            filtersByName.put(columnInfo.name(), columnFilter);
        }
        //build predicate logic for filters
        var filteredData = new FilteredList<>(items, p -> true);
        Map<String, Predicate<?>> predicateMap = new HashMap<>();
        for (var columnInfo : colInfoByName.values()) {
            var filterTextField = filtersByName.get(columnInfo.name());
            filterTextField.predicateProperty().addListener((obs, oldVal, newVal) -> {
                debouncePause.setOnFinished(event -> {
                    predicateMap.put(columnInfo.name(), newVal);
                    filteredData.setPredicate(combinePredicates(predicateMap));
                });
                debouncePause.playFromStart();
            });
        }

        var tableView = new TableView<S>();
        // todo refactor deep columns pass
        TableColumn<S, Integer> actionCol = createActionsColumn(tableView, columns);
        columns.add(0, actionCol);

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
                filter.setFilterTypes(List.of(new SubstringFilterType(), new EqualsFilterType(), new StartsWithFilterType()));
                return filter;
            }
            case NUMBER -> {
                var filter = new AdvancedTextFilter<Number>();
                filter.setFilterTypes(List.of(new NumberEqualsFilter(), new NumberBeforeFilter(), new NumberAfterFilter()));
                return filter;
            }
            case BOOL -> {
                List<String> filterTypes = (List<String>) colProps.get(BOOLEAN_TYPES);
                List<TextFilterType<String>> enumFilterTypes;
                if (filterTypes != null) {
                    enumFilterTypes = filterTypes.stream().map(EnumFilter::new).map(f -> (TextFilterType<String>) f).collect(Collectors.toList());
                    enumFilterTypes.add(0, new AllIncludeEnumFilter());
                } else {
                    enumFilterTypes = List.of(new AllIncludeEnumFilter(), new EnumFilter("Да"), new EnumFilter("Нет"));
                }

                var filter = new AdvancedEnumFilter();
                filter.setFilterTypes(enumFilterTypes);
                return filter;
            }
            case DATE -> {
                // todo Think about implementing more intelligent filters for search by day, month, and year and some patterns.
                // This could impact the search speed, making it slower. Maybe implement some trade-off solution, for example DatePatternFilter
                var filter = new AdvancedDateFilter();
                filter.setFilterTypes(List.of(new DateEqualsFilter(), new DateBeforeFilter(), new DateAfterFilter()));
                return filter;
            }
            case ENUM -> {
                List<String> filterTypes = (List<String>) colProps.get(ENUM_FILTER_TYPES);
                if (filterTypes == null) {
                    throw new IllegalArgumentException("Cannot create enum column filter without prop: %s. Please, pass it via addColumn method.".formatted(ENUM_FILTER_TYPES));
                }
                var filter = new AdvancedEnumFilter();
                List<TextFilterType<String>> enumFilterTypes = filterTypes.stream().map(EnumFilter::new).map(f -> (TextFilterType<String>) f).collect(Collectors.toList());
                enumFilterTypes.add(0, new AllIncludeEnumFilter());
                filter.setFilterTypes(enumFilterTypes);
                return filter;
            }
            default -> throw new IllegalArgumentException("Unknown column type %s".formatted(type));
        }
    }

    /**
     * Создает один предикат на основе всех фильтров-предикатов в колонках таблицы
     */
    private Predicate<S> combinePredicates(Map<String, Predicate<?>> predicateMap) {
        Predicate<S> resPredicate = p -> true;
        for (Map.Entry<String, Predicate<?>> predicateEntry : predicateMap.entrySet()) {
            String colName = predicateEntry.getKey();
            Predicate<?> filterPredicate = predicateEntry.getValue();
            ColumnInfo<S, ?> colInfo = colInfoByName.get(colName);
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
                    return ((Predicate<Object>) filterPredicate).test(
                            mapBoolToString((boolean) value,
                                    (List<String>) props.getOrDefault(colName, new HashMap<>()).get(BOOLEAN_TYPES)));
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
        menuButton.getItems().addAll(
                createClearFiltersMenuItem(columns),
                createChangeTableColumnsMenuItem(background, columns),
                createExportToCsvMenuItem(columns));

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

    private MenuItem createExportToCsvMenuItem(List<TableColumn<S, ?>> columns) {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        var menuItem = new MenuItem("Export to CSV");
        menuItem.setOnAction(exportToCsvHandler == null ? event -> {
            File saveFile = fileChooser.showSaveDialog(new Stage());
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
        result.add(colInfoByName.keySet().stream().map(o -> (Object) o).toList());

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