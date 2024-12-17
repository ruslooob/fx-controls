package com.ruslooob.fxcontrols.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.stage.Popup;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboButton<T> extends Button {
    static int CELL_SIZE = 30;
    static int MAX_HEIGHT_CELLS_COUNT = 4;

    Popup popup = new Popup();
    //todo add enter to submit current selection
    ListView<T> listView = new ListView<>();
    ObjectProperty<Function<T, String>> cellConverterProperty = new SimpleObjectProperty<>(Object::toString);
    //todo add tooltip on current selected item
    ObjectProperty<Function<T, String>> cellTooltipConverterProperty = new SimpleObjectProperty<>(Object::toString);

    public ComboButton() {
        setMinWidth(50);
        popup.setAutoHide(true);
        listView.setFixedCellSize(CELL_SIZE);
        listView.setMaxHeight((CELL_SIZE + 1) * MAX_HEIGHT_CELLS_COUNT);
        listView.prefHeightProperty().bind(listView.fixedCellSizeProperty().multiply(Bindings.size(listView.getItems())).add(5));

        listView.prefWidthProperty().bind(widthProperty());
        popup.getContent().add(listView);

        listView.setCellFactory(lv -> {
            ListCell<T> cell = new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(getCellConverter().apply(item));

                        String tooltipText = getCellTooltipConverter().apply(item);
                        if (!tooltipText.isBlank()) {
                            setTooltip(new Tooltip(tooltipText));
                        }
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        //place listview right under select button
        setOnAction(e -> {
            if (!popup.isShowing()) {
                double x = localToScreen(getLayoutBounds()).getMinX();
                double y = localToScreen(getLayoutBounds()).getMaxY();
                popup.show(this, x, y);
            } else {
                popup.hide();
            }
        });

        //change current selected item if new listview item selected
        listView.setOnMouseClicked(event -> {
            T selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                setText(getCellConverter().apply(selectedItem));
                popup.hide();
            }
        });

        //update selected item while cellConverterProperty has changed
        cellConverterProperty.addListener((obs, oldVal, newVal) -> {
            setText(newVal.apply(listView.getSelectionModel().getSelectedItem() == null
                    ? listView.getItems().get(0)
                    : listView.getSelectionModel().getSelectedItem()));
        });
    }

    public Function<T, String> getCellConverter() {
        return cellConverterProperty.get();
    }

    public void setCellConverter(Function<T, String> cellConverterProperty) {
        this.cellConverterProperty.setValue(cellConverterProperty);
    }

    public void setCellTooltipConverter(Function<T, String> cellTooltipConverter) {
        this.cellTooltipConverterProperty.setValue(cellTooltipConverter);
    }

    public Function<T, String> getCellTooltipConverter() {
        return cellTooltipConverterProperty.get();
    }

    public ObjectProperty<Function<T, String>> cellTooltipConverterProperty() {
        return cellTooltipConverterProperty;
    }

    public void setValue(T value) {
        setText(getCellConverter().apply(value));
    }

    /**
     * Вставляет фильтры в меню выбора, а также выбирает первый элемент в списке в качестве выбранного
     */
    public void setItems(List<T> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Некорректное значение элементов для ComboButton: %s".formatted(values));
        }
        listView.getItems().setAll(values);
        setValue(values.get(0));
        listView.getSelectionModel().selectFirst();
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return listView.getSelectionModel().selectedItemProperty();
    }

    public T getValue() {
        return valueProperty().getValue();
    }
}
