package SkyNetJR.Rendering;

import java.util.List;

import SkyNetJR.GLFWWindowManager.WindowManager;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;

public class RenderThread extends Thread {
    private final View view;
    private boolean destroy;
    private final Object destroyedHandle;
    private boolean usingVSync;

    private long windowHandle;
    private WindowManager wm;

    public RenderThread(View view, WindowManager wm) {
        this.view = view;
        this.wm = wm;
        destroyedHandle = new Object();
    }

    @Override
    public void run(){
        windowHandle = wm.CreateNewWindow(view.getWIDTH(), view.getHEIGHT(), view.getTitle(), null, false, false);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        usingVSync = view.isUseVSync();
        glfwSwapInterval(usingVSync ? 1 : 0);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glViewport(0, 0, view.getWIDTH(), view.getHEIGHT());
        GL11.glOrtho(0, view.getWIDTH(), view.getHEIGHT(), 0, 1d, -1d);
        GL11.glMatrixMode(GL11.GL_VIEWPORT);

        GL11.glClearColor(0f, 0f, 0f, 1f);

        while (true) {
            if (glfwWindowShouldClose(windowHandle)) {
                destroy = true;
                wm.DestroyWindow(windowHandle);

                Runnable cleanUpTask = view::Destroy;
            }

            if (destroy){
                synchronized (destroyedHandle) {
                destroyedHandle.notifyAll();
                }
                return;
            }

            if (usingVSync != view.isUseVSync())
            {
                usingVSync = view.isUseVSync();
                glfwSwapInterval(usingVSync ? 1 : 0);
            }

            List<Renderer> r = view.getRenderers();

            for (int i = 0; i < r.size(); i++){
                r.get(i).Render(0, 0);
            }

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    public void Destroy(){
        destroy = true;
        try {
            destroyedHandle.wait(3000);
            if (this.getState() != State.TERMINATED)
                this.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Object getDestroyedHandle() {
        return destroyedHandle;
    }
}
