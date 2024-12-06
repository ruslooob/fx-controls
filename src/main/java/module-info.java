module com.ruslooob.fxcontrols {
    requires javafx.controls;
    requires jdk.jshell;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.ruslooob.fxcontrols to javafx.fxml;
    exports com.ruslooob.fxcontrols;
}