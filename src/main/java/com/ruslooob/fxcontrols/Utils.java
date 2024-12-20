package com.ruslooob.fxcontrols;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

public class Utils {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");

    /**
     * Пригождается, когда нужно понять, какое пространство занимает элемент.
     */
    public static void addBorder(Region node) {
        node.setBorder(new Border(new BorderStroke(
                Color.web("#000000"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

}
