package Test;

import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.View;

public class Main {
    public static void main(String[] args) {
        WindowManager wm = new WindowManager();
        wm.Init();

        View v = new View(1280, 720, "SkyNetJR::Test", true, wm);
        v.Start();

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
