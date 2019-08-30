package SkyNetJR.Rendering;

import SkyNetJR.GLFWWindowManager.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class View {
    private final int WIDTH;
    private final int HEIGHT;
    private final String title;

    private boolean Destroyed;
    private List<Renderer> renderers;
    private boolean useVSync;
    private RenderThread renderThread;

    public View(int width, int height, String title, WindowManager wm) {
        WIDTH = width;
        HEIGHT = height;
        renderers = new ArrayList<>();
        useVSync = true;
        this.title = title;

        renderThread = new RenderThread(this, wm);
    }

    public boolean getDestroyed() {
        return Destroyed;
    }

    public void Start() {
        renderThread.start();
    }

    public List<Renderer> getRenderers() {
        return renderers;
    }

    public void Destroy() {
        renderThread.Destroy();

        for (Renderer renderer : renderers) {
            renderer.Destroy();
        }

        renderers.clear();
        Destroyed = true;
    }

    public boolean isUseVSync() {
        return useVSync;
    }

    public void setUseVSync(boolean useVSync) {
        this.useVSync = useVSync;
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public String getTitle() {
        return title;
    }
}
