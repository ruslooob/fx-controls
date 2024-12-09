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
import java.util.Random;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.Utils.dateFormatter;

public class TableViewApplication extends Application {
    private static Random random = new Random();

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
                new Person("John", "Doe", 176, LocalDate.parse("1990-01-01"), true),
                new Person("Jane", "Smith", 180, LocalDate.parse("1985-05-15"), false),
                new Person("Mike", "Johnson", 190, LocalDate.parse("2000-12-22"), true)
        );
        TableColumn<Person, String> firstNameCol = createTableColumn("First Name", Person::firstNameProperty);
        TableColumn<Person, String> lastNameCol = createTableColumn("Last Name", Person::lastNameProperty);
        TableColumn<Person, Number> heightCol = createTableColumn("Height", Person::heightProperty);

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

        TableColumn<Person, Boolean> isEmployedCol = createTableColumn("Is Employed", Person::isEmployedProperty);

        var tableView = TableViewBuilder.<Person>builder()
                .addRowNumColumn()
                .addColumn(firstNameCol, ColumnType.STRING)
                .addColumn(lastNameCol, ColumnType.STRING)
                .addColumn(heightCol, ColumnType.NUMBER)
                .addColumn(dateOfBirthCol, ColumnType.DATE)
                .addColumn(isEmployedCol, ColumnType.BOOL)
                .items(data)
                .build();

        Button addButton = new Button("Add Person");
        addButton.setOnAction(event -> {
            Person randomPerson = new Person(
                    "Name" + random.nextInt(100),
                    "Surname" + random.nextInt(100),
                    random.nextInt(150, 210),
                    nextDate(),
                    random.nextBoolean());
            data.add(randomPerson);
//            tableView.refresh();
        });

        VBox layout = new VBox(10, tableView, addButton);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 1500, 600);
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

    private static LocalDate nextDate() {
        LocalDate minDate = LocalDate.of(1950, 1, 1);
        LocalDate maxDate = LocalDate.now();
        int minDay = (int) minDate.toEpochDay();
        int maxDay = (int) maxDate.toEpochDay();
        long randomDay = minDay + random.nextInt(maxDay - minDay);
        return LocalDate.ofEpochDay(randomDay);
    }
}