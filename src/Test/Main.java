package Test;

import SkyNetJR.GLFWWindowManager.WindowManager;

public class Main {
    public static void main(String[] args){
        WindowManager wm = new WindowManager();
        wm.Init();

        long window = wm.CreateNewWindow(1280, 720, "test", null, true, true);

        wm.Destroy();
    }
}
