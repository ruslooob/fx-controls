package com.ruslooob.fxcontrols;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

public class TableViewApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    /*
    Все предикаты должны стакаться
    * колонки могут быть с типом данных: строка, число, дата, (custom)
    * со строками мы можем делать поиск по паттерну %substring% (ignorecase)
    * с числами мы можем приводить такие операции > < = <>
    * */
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", "Doe", LocalDate.parse("1990-01-01")),
                new Person("Jane", "Smith", LocalDate.parse("1985-05-15")),
                new Person("Mike", "Johnson", LocalDate.parse("2000-12-22"))
        );
        TableColumn<Person, String> firstNameCol = createTableColumn("First Name", Person::firstNameProperty);
        TableColumn<Person, String> lastNameCol = createTableColumn("Last Name", Person::lastNameProperty);

        TableColumn<Person, LocalDate> dateOfBirthCol = createTableColumn("Date of Birth", Person::dateOfBirthProperty);
        dateOfBirthCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(dateFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        }));

        var tableView = TableViewBuilder.<Person>builder()
                .addColumn(firstNameCol, ColumnType.STRING)
                .addColumn(lastNameCol, ColumnType.STRING)
                .addColumn(dateOfBirthCol, ColumnType.DATE)
                .items(data)
                .build();

        Button addButton = new Button("Add Person");
        addButton.setOnAction(event -> {
            data.add(new Person("New", "Person", LocalDate.now()));
//            tableView.refresh();
        });

        VBox layout = new VBox(10, tableView, addButton);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("TableView with Inline Filters");
        stage.show();
    }

    private static <S, T> TableColumn<S, T> createTableColumn(String colName, Function<S, Property<T>> propertyGetter) {
        var col = new TableColumn<S, T>(colName);
        col.setPrefWidth(200);
        col.setCellValueFactory(cellData -> propertyGetter.apply(cellData.getValue()));
        return col;
    }
}