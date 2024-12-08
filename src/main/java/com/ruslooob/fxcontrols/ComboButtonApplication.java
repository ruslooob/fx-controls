package com.ruslooob.fxcontrols;

import com.ruslooob.fxcontrols.controls.ComboButton;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class ComboButtonApplication extends Application {
    @Override
    public void start(Stage stage) {
        record Filter(String code, String name) {
        }
        ;
        ComboButton<Filter> comboButton = new ComboButton<>();
        comboButton.setPrefWidth(50);
        List<Filter> values = List.of(new Filter("1", "="), new Filter("2", "!="), new Filter("3", "..."));
        comboButton.setItems(values);
        comboButton.setCellConverterProperty(f -> f.name);
        comboButton.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println(newVal);
        });

        StackPane root = new StackPane(comboButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Set the scene
        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Popup ComboBox Analog with ListView");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
