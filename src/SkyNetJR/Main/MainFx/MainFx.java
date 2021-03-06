/*
* Einstiegspunkt des Projektes
* */

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
        // JavaFX Ansicht initialisieren
        // GUI und Design aus der eingebetteten Datei "fxml/DashboardView/DashboardView.fxml" laden
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/DashboardView/DashboardView.fxml"));
        Parent root = null;
        try {
            root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setTitle("[Dashboard] SkyNetJR - Ludger Halpick");
            primaryStage.setScene(scene);
            primaryStage.setOnHidden(e -> {
                ((DashboardView)loader.getController()).shutdown();
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
