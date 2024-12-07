package com.ruslooob.fxcontrols;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

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
        // todo добавить возможность создавать колонки с шириной
        // todo добавить возможность создавать вложенные колонки
        var tableView = TableViewBuilder.<Person>builder()
                .addColumn("FirstName", ColumnType.STRING, Person::firstNameProperty)
                .addColumn("LastName", ColumnType.STRING, Person::lastNameProperty)
                .addColumn("Date Of Birth", ColumnType.DATE, Person::dateOfBirthProperty)
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
}