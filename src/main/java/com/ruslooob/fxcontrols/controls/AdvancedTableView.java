package com.ruslooob.fxcontrols.controls;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static javafx.collections.FXCollections.observableArrayList;

public class AdvancedTableView<S> extends TableView<S> {
    ObservableList<S> selectedItems = observableArrayList();

    public void setMultiSelect(boolean multiSelect) {
        if (multiSelect) {
            getColumns().add(0, createCheckBoxColumn("Выбрать", selectedItems));
        }
    }

    private TableColumn<S, Boolean> createCheckBoxColumn(String columnName, ObservableList<S> selectedItems) {
        TableColumn<S, Boolean> checkBoxColumn = new TableColumn<>(columnName);

        checkBoxColumn.setCellValueFactory(param -> {
            S rowItem = param.getValue();
            var property = new SimpleBooleanProperty(selectedItems.contains(rowItem));

            property.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selectedItems.add(rowItem);
                } else {
                    selectedItems.remove(rowItem);
                }
            });

            return property;
        });

        checkBoxColumn.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    S rowItem = getTableRow().getItem();
                    if (rowItem != null) {
                        if (checkBox.isSelected()) {
                            selectedItems.add(rowItem);
                        } else {
                            selectedItems.remove(rowItem);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(checkBox);
                    checkBox.setSelected(item != null && item);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        return checkBoxColumn;
    }

    public ObservableList<S> getSelectedItems() {
        return selectedItems;
    }
}
