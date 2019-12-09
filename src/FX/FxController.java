package FX;

import SkyNetJR.AI.NeuralNetwork;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

import java.io.IOException;

public class FxController extends Application implements Runnable {
    private static NeuralNetwork _nn;
    private Slider[] sliders = new Slider[12];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("fxml/controller.fxml"));

            for (int i = 0; i < sliders.length; i++) {
                sliders[i] = (Slider) root.lookup("#" + i);
                int finalI = i;
                sliders[i].valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (_nn != null){
                        _nn.getInputs()[finalI + 1].setValue((double)newValue / 100);
                        _nn.EvaluateCpu();
                    }
                });
            }

            Scene scene = new Scene(root);

            primaryStage.setTitle("NN Controller");
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

    public NeuralNetwork get_nn() {
        return _nn;
    }

    public void set_nn(NeuralNetwork _nn) {
        FxController._nn = _nn;
    }

    @Override
    public void run() {
        launch();
    }
}
