package com.ruslooob.fxcontrols;

import com.ruslooob.fxcontrols.controls.AdvancedTableView;
import com.ruslooob.fxcontrols.enums.ColumnType;
import com.ruslooob.fxcontrols.model.PaginationInfo;
import com.ruslooob.fxcontrols.utils.CellFactoryBuilder;
import com.ruslooob.fxcontrols.utils.Pagination;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import static com.ruslooob.fxcontrols.enums.PropType.ENUM_FILTER_TYPES;
import static javafx.collections.FXCollections.observableArrayList;

public class TableViewApplication extends Application {
    private final Random random = new Random();
    private final static List<String> genders = List.of("М", "Ж", "Undefined");

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        TableColumn<Person, String> firstNameCol = createTableColumn("First Name", Person::firstNameProperty);
        TableColumn<Person, String> lastNameCol = createTableColumn("Last Name", Person::lastNameProperty);
        TableColumn<Person, Number> heightCol = createTableColumn("Height", Person::heightProperty);
        TableColumn<Person, String> infoCol = createTableColumn("Info", Person::infoColProperty);


        TableColumn<Person, LocalDate> dateOfBirthCol = createTableColumn("Date of Birth", Person::dateOfBirthProperty);
        dateOfBirthCol.setCellFactory(
                CellFactoryBuilder.withType(ColumnType.DATE)
                        .copyContextMenu()
                        .build());

        TableColumn<Person, Boolean> isEmployedCol = createTableColumn("Is Employed", Person::isEmployedProperty);
        isEmployedCol.setCellFactory(
                CellFactoryBuilder.withType(ColumnType.BOOL)
                        .copyContextMenu()
                        .build());
        TableColumn<Person, String> genderCol = createTableColumn("Gender", Person::genderProperty);

        TableColumn<Person, LocalTime> createdAtCol = createTableColumn("Created At", Person::createdAtProperty);
        createdAtCol.setCellFactory(
                CellFactoryBuilder.withType(ColumnType.TIME)
                        .build());

        var tableView = new AdvancedTableView<Person>();
        tableView.addColumn(firstNameCol, ColumnType.STRING);
        tableView.addColumn(lastNameCol, ColumnType.STRING);
        tableView.addColumn(heightCol, ColumnType.NUMBER);
        tableView.addColumn(infoCol, ColumnType.STRING);
        tableView.addColumn(dateOfBirthCol, ColumnType.DATE);
        tableView.addColumn(isEmployedCol, ColumnType.BOOL);
        tableView.addColumn(genderCol, ColumnType.ENUM, Map.of(ENUM_FILTER_TYPES, genders));
        tableView.addColumn(createdAtCol, ColumnType.TIME);

        tableView.enableRowNumColumn(true);
        tableView.enableMultiSelect(true);

        ObservableList<Person> data = observableArrayList(createDataSample(100_000));
        tableView.setData(data);

        Button addButton = new Button("Add Person");
        addButton.setOnAction(event -> {
            Person randomPerson = nextPerson();
            data.add(randomPerson);
//            tableView.refresh();
        });

        tableView.getSelectedItems().addListener((ListChangeListener<Person>) c -> {
            ObservableList<Person> items = tableView.getSelectedItems();
            System.out.printf("size=%s list=%s%n", items.size(), items);
        });

//        HBox statusLine = Utils.createStatusLine(tableView);
//        VBox layout = new VBox(10, tableView, statusLine, addButton);
//        layout.setPadding(new Insets(10));

        Pagination<Person, Long> paginationTable = new Pagination<>(tableView,
                new PaginationInfo<>(Person::getId,
                        (pageSize, lastId) -> createDataSample(pageSize),
                        (pageSize, lastId) -> createDataSample(pageSize)),
                100);

        Scene scene = new Scene(paginationTable, 1500, 600);
        stage.setScene(scene);
        stage.setTitle("TableView with Inline Filters");
        stage.show();
    }

    private ObservableList<Person> createDataSample(int size) {
        ObservableList<Person> data = observableArrayList();
        //таблица нормально работает на 100_000 записях
        for (int i = 0; i < size; i++) {
            data.add(nextPerson());
        }
        return data;
    }

    static long currId = 0;

    private Person nextPerson() {
        return new Person(
                currId++,
                nextElem(names) + random.nextInt(100),
                nextElem(surnames) + random.nextInt(100),
                random.nextInt(210),
                nextDate(),
                random.nextBoolean(),
                nextElem(genders),
                nextTime(),
                UUID.randomUUID().toString()
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

    private LocalDate nextDate() {
        LocalDate minDate = LocalDate.of(1950, 1, 1);
        LocalDate maxDate = LocalDate.now();
        int minDay = (int) minDate.toEpochDay();
        int maxDay = (int) maxDate.toEpochDay();
        long randomDay = minDay + random.nextInt(maxDay - minDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    private LocalTime nextTime() {
        int maxSecond = 24 * 60 * 60 - 1; // 23:59:59
        int randomSecond = random.nextInt(maxSecond + 1);
        return LocalTime.ofSecondOfDay(randomSecond);
    }

    private <T> T nextElem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}