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
    private ReentrantLock WindowHandlesLock = new ReentrantLock();
    private List<Long> WindowHandles;

    public WindowManager() {
        WindowHandles = new ArrayList<>();
    }

    // GLFW initialisieren
    public void Init() {
        GLFWErrorCallback.createPrint(System.err);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
    }

    public long CreateNewWindow(int width, int height, String title, GLFWKeyCallback keyCallback, boolean resizable, boolean createHidden) {
        glfwWindowHint(GLFW_VISIBLE, createHidden ? GLFW_FALSE : GLFW_TRUE); // Legt fest, ob das Fenster versteckt bleiben soll
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE); // Legt fest, ob der Nutzer die Größe des Fensters bearbeiten kann

        long window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback);

        WindowHandlesLock.lock();
        WindowHandles.add(window);
        WindowHandlesLock.unlock();

        return window;
    }

    // Zerstört bestimmtes Fenster und räumt Arbeitsspeicher auf
    public void DestroyWindow(long window) {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        WindowHandlesLock.lock();
        for (int i = 0; i < WindowHandles.size(); i++)
            if (WindowHandles.get(i) == window) {
                WindowHandles.remove(i);
                break;
            }
        WindowHandlesLock.unlock();
    }

    // Getter
    public Long[] GetWindowHandles() {
        return (Long[]) WindowHandles.toArray();
    }

    // Zerstört WindowManager und alle seine Fenster und räumt Arbeitsspeicher
    public void Destroy() {
        for (int i = WindowHandles.size() - 1; i >= 0; i--)
            DestroyWindow(WindowHandles.get(i));

        glfwTerminate();
        try {
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        } catch (NullPointerException e) {
            // ignore
        }
    }
}
