package com.ruslooob.fxcontrols;

import com.ruslooob.fxcontrols.column_utils.CellFactoryBuilder;
import com.ruslooob.fxcontrols.controls.TableViewBuilder;
import com.ruslooob.fxcontrols.enums.ColumnType;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.enums.PropType.ENUM_FILTER_TYPES;
import static javafx.collections.FXCollections.observableArrayList;

public class TableViewApplication extends Application {
    private static Random random = new Random();
    private static List<String> genders = List.of("М", "Ж", "Undefined");

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        // Используется в некоторых контролах, например DatePicker
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));
        ObservableList<Person> data = observableArrayList();
        //таблица нормально работает на 100_000 записях
        for (int i = 0; i < 1_000; i++) {
            data.add(nextPerson());
        }
        TableColumn<Person, String> firstNameCol = createTableColumn("First Name", Person::firstNameProperty);
        TableColumn<Person, String> lastNameCol = createTableColumn("Last Name", Person::lastNameProperty);
        TableColumn<Person, Number> heightCol = createTableColumn("Height", Person::heightProperty);

        TableColumn<Person, LocalDate> dateOfBirthCol = createTableColumn("Date of Birth", Person::dateOfBirthProperty);
        dateOfBirthCol.setCellFactory(CellFactoryBuilder.builder()
                .type(ColumnType.DATE)
                .copyContextMenu()
                .build());

        TableColumn<Person, Boolean> isEmployedCol = createTableColumn("Is Employed", Person::isEmployedProperty);
        isEmployedCol.setCellFactory(CellFactoryBuilder.builder()
                .type(ColumnType.BOOL)
                .copyContextMenu()
                .build());
        TableColumn<Person, String> genderCol = createTableColumn("Gender", Person::genderProperty);

        var tableView = TableViewBuilder.<Person>builder()
                .addRowNumColumn()
                .addColumn(firstNameCol, ColumnType.STRING)
                .addColumn(lastNameCol, ColumnType.STRING)
                .addColumn(heightCol, ColumnType.NUMBER)
                .addColumn(dateOfBirthCol, ColumnType.DATE)
                .addColumn(isEmployedCol, ColumnType.BOOL)
                .addColumn(genderCol, ColumnType.ENUM, Map.of(ENUM_FILTER_TYPES, genders))
                .items(data)
                .build();

        Button addButton = new Button("Add Person");
        addButton.setOnAction(event -> {
            Person randomPerson = nextPerson();
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

    private static Person nextPerson() {
        return new Person(
                nextElem(names) + random.nextInt(0, 100),
                nextElem(surnames) + random.nextInt(0, 100),
                random.nextInt(150, 210),
                nextDate(),
                random.nextBoolean(),
                nextElem(genders)
        );
    }
    private final static List<String> names = List.of(
            "John", "Alice", "Bob", "Sophia", "Michael", "Emma", "James", "Olivia", "David", "Mia",
            "Daniel", "Isabella", "Matthew", "Charlotte", "Alexander", "Amelia", "Ethan", "Harper",
            "William", "Ella"
    );

    private final static List<String> surnames = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez",
            "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
            "Jackson", "Martin"
    );

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

    private static <T> T nextElem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}