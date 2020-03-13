/*
* Klasse, die einen Kontext für Fenster bereit stellt.
* */

package SkyNetJR.Graphics.Rendering;

import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Threading.WindowThread;

import java.util.ArrayList;
import java.util.List;

public class View {
    // Eigenschaften des Fensterkontextes
    private int width;
    private int height;
    private final boolean resizeable;
    private final String title;

    private boolean Destroyed;
    private List<Renderer> renderers;
    private boolean useVSync;
    private WindowThread _windowThread;

    public View(int width, int height, String title, boolean resizeable, WindowManager wm) {
        this.width = width;
        this.height = height;
        renderers = new ArrayList<>();
        useVSync = true;
        this.title = title;
        this.resizeable = resizeable;

        _windowThread = new WindowThread(this, wm);
    }

    // Kontext bzw. Thread des Fensters starten
    public void Start() {
        _windowThread.start();
    }

    // Getter und Setter
    public List<Renderer> getRenderers() {
        return renderers;
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
    public void setWidth(int w) {
        width = w;
    }

    public int getHeight() {
        return height;
    }
    public void setHeight(int h) {
        height = h;
    }

    public String getTitle() {
        return title;
    }

    public boolean getResizable() { return resizeable; }

    public void setClosable(boolean closable) { _windowThread.setAllowClosing(closable); }
    public boolean isClosable() { return _windowThread.isAllowClosing(); }

    public void setVisible(boolean _visible) {
        this._windowThread.setVisible(_visible);
    }
    public boolean isVisible() {
        return _windowThread.isVisible();
    }

    public boolean getDestroyed() {
        return Destroyed;
    }

    // View zerstören und Arbeitsspeicher aufräumen
    public void Destroy() {
        _windowThread.Destroy();

        for (Renderer renderer : renderers) {
            renderer.Destroy();
        }

        renderers.clear();
        Destroyed = true;
    }
}
