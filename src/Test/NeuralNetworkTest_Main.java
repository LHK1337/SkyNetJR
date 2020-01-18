package Test;

import FX.FxController;
import SkyNetJR.Creatures.Creature;
import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderers.BrainRenderer;
import SkyNetJR.Graphics.Rendering.View;

public class NeuralNetworkTest_Main {
    public static void main(String[] args) {
        WindowManager wm = new WindowManager();
        wm.Init();

        View v = new View(960, 480, "SkyNetJR::Test", true, wm);

        Creature c = new Creature(0, 0, null);
        for (int i = 1; i < c.getBrain().getInputs().length; i++) {
            c.getBrain().getInputs()[i].setValue(0d);
        }

        c.getBrain().AddHiddenLayer(8, false);
        //c.getBrain().AddHiddenLayer(7, false);
        c.getBrain().AddHiddenLayer(13, false);
        //c.getBrain().AddHiddenLayer(9, false);
        //c.getBrain().AddHiddenLayer(10, true);
        c.getBrain().AddHiddenLayer(6, true);

        c.getBrain().EvaluateCpu();

        v.getRenderers().add(new BrainRenderer(c.getBrain()));

        v.Start();

        FxController nnc = new FxController();
        nnc.set_nn(c.getBrain());
        new Thread(nnc).start();

        while (!v.getDestroyed()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        wm.Destroy();
    }
}
