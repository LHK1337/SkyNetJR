package SkyNetJR.Graphics.Rendering;

import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class View {
    private int width;
    private int height;
    private final boolean resizeable;
    private final String title;

    private boolean Destroyed;
    private List<Renderer> renderers;
    private boolean useVSync;
    private RenderThread renderThread;

    public View(int width, int height, String title, boolean resizeable, WindowManager wm) {
        this.width = width;
        this.height = height;
        renderers = new ArrayList<>();
        useVSync = true;
        this.title = title;
        this.resizeable = resizeable;

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public String getTitle() {
        return title;
    }

    public boolean getResizable() { return resizeable; }
}
