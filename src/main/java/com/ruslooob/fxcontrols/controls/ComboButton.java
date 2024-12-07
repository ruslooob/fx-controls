package com.ruslooob.fxcontrols.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Popup;

import java.util.List;
import java.util.function.Function;

public class ComboButton<T> extends Button {
    private static final int CELL_SIZE = 30;
    private static final int MAX_HEIGHT_CELLS_COUNT = 4;

    private final Popup popup = new Popup();
    private final ListView<T> listView = new ListView<>();

    private Function<T, String> cellConverter = Object::toString;

    public ComboButton() {
        setPrefWidth(50);

        listView.setFixedCellSize(CELL_SIZE);
        listView.setMaxHeight((CELL_SIZE + 1) * MAX_HEIGHT_CELLS_COUNT);
        listView.prefHeightProperty().bind(listView.fixedCellSizeProperty().multiply(Bindings.size(listView.getItems())).add(5));

        listView.prefWidthProperty().bind(prefWidthProperty());
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
                        setText(cellConverter.apply(item));
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
                setText(cellConverter.apply(selectedItem));
                popup.hide();
            }
        });
    }

    public void setValue(T value) {
        setText(cellConverter.apply(value));
    }

    public void setItems(List<T> values) {
        listView.getItems().setAll(values);
    }
    // fixme cellConverter not applying if setValue called before
    public void setCellConverter(Function<T, String> cellConverter) {
        this.cellConverter = cellConverter;
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return listView.getSelectionModel().selectedItemProperty();
    }
}
