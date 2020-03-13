/*
* Kontextthread für eigene Fenster
* */

package SkyNetJR.Threading;

import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderer;
import SkyNetJR.Graphics.Rendering.View;
import SkyNetJR.Utils.Stopwatch;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class WindowThread extends DestroyableThread {
    // Eigenschaften
    private final View view;
    private boolean useVSync;
    private boolean _allowClosing;
    private boolean _visible;

    private long windowHandle;
    private WindowManager windowManager;

    private long _renderTime;

    public WindowThread(View view, WindowManager wm) {
        this.view = view;
        this.windowManager = wm;
        _allowClosing = true;
    }

    @Override
    public void run() {
        // Thread benennen
        Thread.currentThread().setName("RenderThread - " + view.toString());

        // Fenster erstellen
        windowHandle = windowManager.CreateNewWindow(view.getWidth(), view.getHeight(), view.getTitle(), null, view.getResizable(), true);
        glfwSetWindowCloseCallback(windowHandle, window -> {
            glfwSetWindowShouldClose(window, this._allowClosing);

            if (!this._allowClosing) setVisible(false);
        });

        // Fensterkontext auf diesen Thread legen
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        // Einstellungen der Ansicht setzen
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

        Stopwatch renderStopwatch = new Stopwatch();

        // Main-Loop der Ansicht
        while (true) {
            renderStopwatch.start();

            // Überprüft, ob sich die Ansicht schließen soll
            if (glfwWindowShouldClose(windowHandle)) {
                destroy = true;
                windowManager.DestroyWindow(windowHandle);

                // Weiteren Thread erstellen, der den aktuellen aufräumt
                Thread cleanUpThread = new Thread(() -> {
                    Thread.currentThread().setName("CleanUpThread");
                    view.Destroy();
                });
                cleanUpThread.start();
            }

            // Überprüfen, ob der Thread beendet werden soll
            if (destroy) {
                synchronized (destroyedHandle) {
                    destroyedHandle.notifyAll();
                }
                return; // Thread beenden
            }

            // Vertikale Synchronisation einstellen
            if (useVSync != view.isUseVSync()) {
                useVSync = view.isUseVSync();
                glfwSwapInterval(useVSync ? 1 : 0);
            }

            // Ansicht leeren
            glClear(GL_COLOR_BUFFER_BIT);

            // Alle Renderer der Ansicht nach einander abarbeiten
            try {
                for (Renderer renderer : view.getRenderers()) {
                    // Renderer ausführen
                    renderer.Render(renderer.getPositionX(), renderer.getPositionY());
                }
            } catch (Exception e)
            {
                System.out.println("[EXCEPTION] while rendering");
                e.printStackTrace();
            }

            // Anzeigebuffer wechseln
            glfwSwapBuffers(windowHandle);

            // Fensterevents abrufen
            glfwPollEvents();

            // Renderzeit ermitteln
            renderStopwatch.end();
            _renderTime = renderStopwatch.getTotalTime();
        }
    }

    // Ansicht auf Fenstergröße strecken/stauchen
    private void resizeViewPortToWindow(){
        GL11.glViewport(0, 0, view.getWidth(), view.getHeight());
    }

    // Getter und Setter
    public boolean isAllowClosing() {
        return _allowClosing;
    }
    public void setAllowClosing(boolean _allowClosing) {
        this._allowClosing = _allowClosing;
    }

    public boolean isVisible() {
        return _visible;
    }
    public void setVisible(boolean visible){
        _visible = visible;

        if (visible)
            glfwShowWindow(windowHandle);
        else
            glfwHideWindow(windowHandle);
    }

    public long getWindowHandle() { return windowHandle; }

    public long getRenderTime(){
        return _renderTime;
    }

    @Override
    public void Destroy() {
        windowManager.DestroyWindow(windowHandle);

        super.Destroy();
    }
}
