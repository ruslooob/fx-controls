package com.ruslooob.fxcontrols.utils;

import com.ruslooob.fxcontrols.controls.AdvancedTableView;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

public class Utils {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Пригождается, когда нужно понять, какое пространство занимает элемент.
     */
    public static void addBorder(Region node) {
        node.setBorder(new Border(new BorderStroke(
                Color.web("#000000"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public static <T> HBox createStatusLine(AdvancedTableView<T> tableView) {
        var selectedCountLabel = new Label("Выбрано: 0");
        var totalCountLabel = new Label("Всего: " + tableView.getItems().size());

        var statusLine = new HBox(10);
        statusLine.getChildren().addAll(selectedCountLabel, totalCountLabel);

        tableView.getSelectedItems().addListener((ListChangeListener<T>) change -> {
            int selectedCount = tableView.getSelectedItems().size();
            selectedCountLabel.setText("Выбрано: " + selectedCount);
        });

        tableView.getItems().addListener((ListChangeListener<T>) change -> {
            int totalCount = tableView.getItems().size();
            totalCountLabel.setText("Всего: " + totalCount);
        });

        return statusLine;
    }

}
