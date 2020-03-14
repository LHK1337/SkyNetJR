/*
* Helferklasse, die das Erstellen von Fenstern übernimmt.
* */

package SkyNetJR.Graphics.GLFWWindowManager;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowManager {
    private ReentrantLock _windowHandlesLock = new ReentrantLock();
    private List<Long> _windowHandles;

    public WindowManager() {
        _windowHandles = new ArrayList<>();
    }

    // GLFW initialisieren
    public void init() {
        GLFWErrorCallback.createPrint(System.err);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
    }

    public long createNewWindow(int width, int height, String title, GLFWKeyCallback keyCallback, boolean resizable, boolean createHidden) {
        glfwWindowHint(GLFW_VISIBLE, createHidden ? GLFW_FALSE : GLFW_TRUE); // Legt fest, ob das Fenster versteckt bleiben soll
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE); // Legt fest, ob der Nutzer die Größe des Fensters bearbeiten kann

        long window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback);

        _windowHandlesLock.lock();
        _windowHandles.add(window);
        _windowHandlesLock.unlock();

        return window;
    }

    // Zerstört bestimmtes Fenster und räumt Arbeitsspeicher auf
    public void destroyWindow(long window) {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        _windowHandlesLock.lock();
        for (int i = 0; i < _windowHandles.size(); i++)
            if (_windowHandles.get(i) == window) {
                _windowHandles.remove(i);
                break;
            }
        _windowHandlesLock.unlock();
    }

    // Getter
    public Long[] getWindowHandles() {
        return (Long[]) _windowHandles.toArray();
    }

    // Zerstört WindowManager und alle seine Fenster und räumt Arbeitsspeicher
    public void destroy() {
        for (int i = _windowHandles.size() - 1; i >= 0; i--)
            destroyWindow(_windowHandles.get(i));

        glfwTerminate();
        try {
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        } catch (NullPointerException e) {
            // ignore
        }
    }
}
