module com.ruslooob.fxcontrols {
    requires javafx.controls;
    requires jdk.jshell;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires static lombok;

    opens com.ruslooob.fxcontrols to javafx.fxml;
    exports com.ruslooob.fxcontrols;
    exports com.ruslooob.fxcontrols.filters;
    exports com.ruslooob.fxcontrols.filters.string;
    exports com.ruslooob.fxcontrols.filters.date;
}