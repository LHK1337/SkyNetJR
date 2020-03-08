package SkyNetJR.Main.MainFx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("fxml/DashboardView/DashboardView.fxml"));

            Scene scene = new Scene(root);

            primaryStage.setTitle("[Dashboard] SkyNetJR - Ludger Halpick");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
