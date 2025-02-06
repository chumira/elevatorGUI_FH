module elevator.elevatorgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;
    requires lombok;

    opens gui to javafx.fxml;
    exports gui;
}