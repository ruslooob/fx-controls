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
import com.ruslooob.fxcontrols.model.Pair;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ruslooob.fxcontrols.enums.PropType.BOOLEAN_TYPES;
import static com.ruslooob.fxcontrols.enums.PropType.ENUM_FILTER_TYPES;

@SuppressWarnings("unchecked")
public class TableViewBuilder<S> {
    private static final String DEFAULT_BOOL_TRUE_STR = "ДА";
    private static final String DEFAULT_BOOL_FALSE_STR = "Нет";

    // метаданные колонок, из которых потом будут строиться колонка с умными фильтрами
    private Map<String, ColumnInfo<S, ?>> colInfoByName = new LinkedHashMap<>();
    private boolean enableRowNumCol = false;
    private ObservableList<S> items;
    //some properties which can be used while constructing column filters. Map<colName, Map<PropType, Object>>
    private Map<String, Map<PropType, Object>> props = new HashMap<>();

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
        if (enableRowNumCol) {
            TableColumn<S, Integer> numCol = new TableColumn<>("№");
            numCol.setCellValueFactory(cellData -> {
                int index = tableView.getItems().indexOf(cellData.getValue()) + 1;
                return new SimpleObjectProperty<>(index);
            });
            numCol.setSortable(false);
            columns.add(0, numCol);
        }
        tableView.getColumns().addAll(columns);

        //build sorting logic
        var sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        return tableView;
    }

    private <T> Pair<TableColumn<S, T>, AdvancedTextFilter<?>> createFilterColumn(ColumnInfo<S, T> columnInfo) {
        TableColumn<S, T> col = columnInfo.getColumn();

        AdvancedTextFilter<?> filterTextField = createFilter(columnInfo.getType(), props.getOrDefault(columnInfo.getName(), new HashMap<>()));
        var colNameWithFilterVBox = new VBox(new Label(columnInfo.name()), filterTextField);
        colNameWithFilterVBox.setPadding(new Insets(5));
        col.setText("");
        col.setGraphic(colNameWithFilterVBox);
        return new Pair<>(col, filterTextField);
    }

    //todo add borders for debug purposes
    private AdvancedTextFilter<?> createFilter(ColumnType type, Map<PropType, Object> colProps) {
        switch (type) {
            case STRING -> {
                var filterTextField = new AdvancedTextFilter<String>();
                filterTextField.setFilterTypes(List.of(new SubstringFilterType(), new EqualsFilterType(), new StartsWithFilterType()));
                return filterTextField;
            }
            case NUMBER -> {
                var filterTextField = new AdvancedTextFilter<Number>();
                filterTextField.setFilterTypes(List.of(new NumberEqualsFilter(), new NumberBeforeFilter(), new NumberAfterFilter()));
                return filterTextField;
            }
            case BOOL -> {
                // для BOOL исключение String вместо Boolean
                List<String> filterTypes = (List<String>) colProps.get(ENUM_FILTER_TYPES);
                List<TextFilterType<String>> enumFilterTypes;
                if (filterTypes != null) {
                    enumFilterTypes = filterTypes.stream()
                            .map(EnumFilter::new)
                            .map(f -> (TextFilterType<String>) f)
                            .collect(Collectors.toList());
                    enumFilterTypes.add(0, new AllIncludeEnumFilter());
                } else {
                    enumFilterTypes = List.of(new AllIncludeEnumFilter(), new EnumFilter("Да"), new EnumFilter("Нет"));
                }

                var filterTextField = new AdvancedTextFilter<String>();
                filterTextField.setTextFilterVisible(false);
                filterTextField.setFilterTypes(enumFilterTypes);
                return filterTextField;
            }
            case DATE -> {
                //todo add dateControl for user input
                var filterTextField = new AdvancedTextFilter<LocalDate>();
                filterTextField.setFilterTypes(List.of(new DateEqualsFilter(), new DateBeforeFilter(), new DateAfterFilter()));
                return filterTextField;
            }
            case ENUM -> {
                List<String> filterTypes = (List<String>) colProps.get(ENUM_FILTER_TYPES);
                if (filterTypes == null) {
                    throw new IllegalArgumentException("Cannot create enum column filter without prop: %s. Please, pass it via addColumn method.".formatted(ENUM_FILTER_TYPES));
                }
                var filterTextField = new AdvancedTextFilter<String>();
                filterTextField.setTextFilterVisible(false);
                List<TextFilterType<String>> enumFilterTypes = filterTypes.stream()
                        .map(EnumFilter::new)
                        .map(f -> (TextFilterType<String>) f)
                        .collect(Collectors.toList());
                enumFilterTypes.add(0, new AllIncludeEnumFilter());
                filterTextField.setFilterTypes(enumFilterTypes);
                return filterTextField;
            }
            default -> throw new IllegalArgumentException("Unknown column type %s".formatted(type));
        }
    }

    /**
     * Создает один предикат на основе всех фильтров-предикатов в колонках таблицы
     */
    private Predicate<S> combinePredicates(Map<String, Predicate<?>> predicateMap) {
        Predicate<S> resPredicate = p -> true;
        // у нас есть boolean-поле, которое получилось в ходе применения propertyGetter замапить это на новый предикат, который будет принимать булеан и возвращать строку
        for (Map.Entry<String, Predicate<?>> predicateEntry : predicateMap.entrySet()) {
            String colName = predicateEntry.getKey();
            Predicate<?> filterPredicate = predicateEntry.getValue();
            ColumnInfo<S, ?> colInfo = colInfoByName.get(colName);
            Function<S, ? extends Property<?>> propertyGetter = colInfo.getPropertyGetter();
            Predicate<S> predicate = record -> {
                Property<?> property = propertyGetter.apply(record);
                if (property == null) {
                    return false;
                }
                Object value = property.getValue();
                if (colInfo.getType() != ColumnType.BOOL) {
                    return ((Predicate<Object>) filterPredicate).test(value);
                } else {
                    return ((Predicate<Object>) filterPredicate).test(mapBoolToString((boolean) value, props.getOrDefault(colName, new HashMap<>())));
                }
            };
            resPredicate = resPredicate.and(predicate);
        }

        return resPredicate;
    }

    //todo refactor and passFilterTypes
    private String mapBoolToString(boolean val, Map<PropType, Object> colProps) {
        List<String> filterTypes = (List<String>) colProps.get(BOOLEAN_TYPES);

        if (filterTypes == null) {
            return val ? DEFAULT_BOOL_TRUE_STR : DEFAULT_BOOL_FALSE_STR;
        } else {
            if (filterTypes.size() != 2) {
                throw new IllegalArgumentException("Incorrect size of %s: it must be 2, given %s".formatted(BOOLEAN_TYPES, filterTypes));
            }
            return val ? filterTypes.get(0) : filterTypes.get(1);
        }
    }

}
