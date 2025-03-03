package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        GuiController controller = fxmlLoader.getController();
        stage.setMinWidth(1000);
        stage.setMinHeight(500);
        stage.setTitle("Elevator GUI");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            controller.onClose();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}