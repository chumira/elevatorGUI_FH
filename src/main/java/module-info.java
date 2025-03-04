module elevator.elevatorgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires lombok;
    exports gui;
    opens gui to javafx.fxml;
}