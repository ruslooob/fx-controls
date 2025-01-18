package com.ruslooob.fxcontrols.utils;

import com.ruslooob.fxcontrols.controls.AdvancedTableView;
import com.ruslooob.fxcontrols.model.PaginationInfo;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class Pagination<T, S> extends VBox {
    private final AdvancedTableView<T> tableView;
    private final PaginationInfo<T, S> pagination;
    @Getter
    private int currPage;
    @Getter
    private int pageSize;

    public Pagination(AdvancedTableView<T> tableView, PaginationInfo<T, S> pagination, int initialPageSize) {
        //todo enable status line if tableview is multiselect
        this.tableView = tableView;
        this.pagination = pagination;
        this.currPage = 1;
        this.pageSize = initialPageSize;

        var prevButton = new Button("<");
        var nextButton = new Button(">");
        var currentPageLabel = new Label("Страница: " + currPage);

        ComboBox<Integer> rowsPerPageComboBox = new ComboBox<>();
        rowsPerPageComboBox.getItems().addAll(10, 20, 50, 100);//todo add custom user values
        rowsPerPageComboBox.setValue(pageSize);

        rowsPerPageComboBox.setOnAction(e -> {
            pageSize = rowsPerPageComboBox.getValue();
            loadPage();
        });

        var paginationControls = new HBox(10, rowsPerPageComboBox, prevButton, currentPageLabel, nextButton);
        paginationControls.setAlignment(Pos.CENTER_RIGHT);
        paginationControls.setPadding(new Insets(0, 10, 0, 10));

        prevButton.setOnAction(e -> {
            if (currPage > 1) {
                currPage--;
                loadPage();
                currentPageLabel.setText("Страница: " + currPage);
            }
        });

        nextButton.setOnAction(e -> {
            currPage++;
            loadPage();
            currentPageLabel.setText("Страница: " + currPage);
        });

        setSpacing(10);
        getChildren().addAll(tableView, paginationControls);
    }

    private void loadPage() {
        S lastId = pagination.idFunction().apply(tableView.getItems().get(tableView.getItems().size() - 1));
        ObservableList<T> data = pagination.nextPageFunction().apply(pageSize, lastId);
        tableView.setData(data);
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        loadPage();
    }
}