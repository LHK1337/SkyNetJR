package SkyNetJR.Main.MainFx;

import SkyNetJR.Main.MainFx.fxml.DashboardView.DashboardView;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/DashboardView/DashboardView.fxml"));
        Parent root = null;
        try {
            root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setTitle("[Dashboard] SkyNetJR - Ludger Halpick");
            primaryStage.setScene(scene);
            primaryStage.setOnHidden(e -> {
                ((DashboardView)loader.getController()).Shutdown();
            });
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
