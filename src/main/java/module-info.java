module elevator.elevatorgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;
    requires lombok;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    opens gui to javafx.fxml;
    exports gui;
}