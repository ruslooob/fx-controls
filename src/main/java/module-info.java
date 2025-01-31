module com.ruslooob.fxcontrols {
    requires javafx.controls;
    requires jdk.jshell;
    requires javafx.graphics;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires org.controlsfx.controls;

    exports com.ruslooob.fxcontrols to javafx.graphics;

    exports com.ruslooob.fxcontrols.filters;
    exports com.ruslooob.fxcontrols.filters.string;
    exports com.ruslooob.fxcontrols.filters.date;
    exports com.ruslooob.fxcontrols.enums;
    exports com.ruslooob.fxcontrols.model;
    exports com.ruslooob.fxcontrols.controls;
    exports com.ruslooob.fxcontrols.utils to javafx.graphics;
}