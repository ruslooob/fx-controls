package com.ruslooob.fxcontrols;

import com.ruslooob.fxcontrols.controls.AdvancedTextFilter;
import com.ruslooob.fxcontrols.filters.string.EqualsFilterType;
import com.ruslooob.fxcontrols.filters.string.StartsWithFilterType;
import com.ruslooob.fxcontrols.filters.string.SubstringFilterType;
import com.ruslooob.fxcontrols.filters.TextFilterType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class AdvancedFilterApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var advancedFilter = new AdvancedTextFilter<String>();
        List<TextFilterType<String>> filterTypes = List.of(new SubstringFilterType(), new EqualsFilterType(), new StartsWithFilterType());
        advancedFilter.setFilterTypes(filterTypes);

        Scene scene = new Scene(advancedFilter, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
