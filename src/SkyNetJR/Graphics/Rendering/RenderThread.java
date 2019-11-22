package SkyNetJR.Graphics.Rendering;

import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Utils.DestroyableThread;
import SkyNetJR.Utils.Timer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class RenderThread extends DestroyableThread {
    private final View view;
    private boolean useVSync;

    private long windowHandle;
    private WindowManager windowManager;

    private long _renderTime;

    public long getRenderTime(){
        return _renderTime;
    }

    public RenderThread(View view, WindowManager wm) {
        this.view = view;
        this.windowManager = wm;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("RenderThread - " + view.toString());

        windowHandle = windowManager.CreateNewWindow(view.getWidth(), view.getHeight(), view.getTitle(), null, view.getResizable(), false);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        useVSync = view.isUseVSync();
        glfwSwapInterval(useVSync ? 1 : 0);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        resizeViewPortToWindow();
        GL11.glOrtho(0, view.getWidth(), view.getHeight(), 0, 1d, -1d);
        GL11.glMatrixMode(GL11.GL_VIEWPORT);

        GL11.glClearColor(0f, 0f, 0f, 1f);

        glfwSetWindowSizeCallback(windowHandle, (window, width, height) -> {
            view.setHeight(height);
            view.setWidth(width);
            resizeViewPortToWindow();
        });

        Timer renderTimer = new Timer();

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

            renderTimer.start();

            //glClear(GL_COLOR_BUFFER_BIT);

            for (int i = 0; i < r.size(); i++) {
                r.get(i).Render(0, 0);
            }

            renderTimer.end();
            _renderTime = renderTimer.getTotalTime();

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    private void resizeViewPortToWindow(){
        GL11.glViewport(0, 0, view.getWidth(), view.getHeight());
    }
}
