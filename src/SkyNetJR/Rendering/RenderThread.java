package SkyNetJR.Rendering;

import SkyNetJR.GLFWWindowManager.WindowManager;
import SkyNetJR.Utils.DestroyableThread;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class RenderThread extends DestroyableThread {
    private final View view;
    private boolean useVSync;

    private long windowHandle;
    private WindowManager windowManager;

    public RenderThread(View view, WindowManager wm) {
        this.view = view;
        this.windowManager = wm;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("RenderThread - " + view.toString());

        windowHandle = windowManager.CreateNewWindow(view.getWIDTH(), view.getHEIGHT(), view.getTitle(), null, false, false);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        useVSync = view.isUseVSync();
        glfwSwapInterval(useVSync ? 1 : 0);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glViewport(0, 0, view.getWIDTH(), view.getHEIGHT());
        GL11.glOrtho(0, view.getWIDTH(), view.getHEIGHT(), 0, 1d, -1d);
        GL11.glMatrixMode(GL11.GL_VIEWPORT);

        GL11.glClearColor(0f, 0f, 0f, 1f);

        while (true) {
            if (glfwWindowShouldClose(windowHandle)) {
                destroy = true;
                windowManager.DestroyWindow(windowHandle);

                Thread cleanUpThread = new Thread(() -> {
                    Thread.currentThread().setName("CleanUpThread");
                    view.Destroy();
                });
                cleanUpThread.start();
            }

            if (destroy) {
                synchronized (destroyedHandle) {
                    destroyedHandle.notifyAll();
                }
                return;
            }

            if (useVSync != view.isUseVSync()) {
                useVSync = view.isUseVSync();
                glfwSwapInterval(useVSync ? 1 : 0);
            }

            List<Renderer> r = view.getRenderers();

            for (int i = 0; i < r.size(); i++) {
                r.get(i).Render(0, 0);
            }

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }
}
