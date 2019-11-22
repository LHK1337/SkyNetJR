package SkyNetJR.Main;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class KeyInputs {
    public static void KeyHandler (long window, int key, int scanCode, int action, int mods){
        if (!Main.WorldView.isWindow(window)) return;

        switch (key){
            case GLFW_KEY_SPACE:
                if (action == GLFW_PRESS) Main.SimulationThread.setRealTime(!Main.SimulationThread.isRealTime());
        }
    }
}
