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
    private int _width;
    private int _height;
    private final boolean _resizeable;
    private final String _title;

    private boolean _destroyed;
    private List<Renderer> _renderers;
    private boolean _useVSync;
    private WindowThread _windowThread;

    public View(int width, int height, String title, boolean resizeable, WindowManager wm) {
        this._width = width;
        this._height = height;
        _renderers = new ArrayList<>();
        _useVSync = true;
        this._title = title;
        this._resizeable = resizeable;

        _windowThread = new WindowThread(this, wm);
    }

    // Kontext bzw. Thread des Fensters starten
    public void Start() {
        _windowThread.start();
    }

    // Getter und Setter
    public List<Renderer> getRenderers() {
        return _renderers;
    }

    public boolean isUseVSync() {
        return _useVSync;
    }
    public void setUseVSync(boolean useVSync) {
        this._useVSync = useVSync;
    }

    public int getWidth() {
        return _width;
    }
    public void setWidth(int w) {
        _width = w;
    }

    public int getHeight() {
        return _height;
    }
    public void setHeight(int h) {
        _height = h;
    }

    public String getTitle() {
        return _title;
    }

    public boolean getResizable() { return _resizeable; }

    public void setClosable(boolean closable) { _windowThread.setAllowClosing(closable); }
    public boolean isClosable() { return _windowThread.isAllowClosing(); }

    public void setVisible(boolean _visible) {
        this._windowThread.setVisible(_visible);
    }
    public boolean isVisible() {
        return _windowThread.isVisible();
    }

    public boolean getDestroyed() {
        return _destroyed;
    }

    // View zerstören und Arbeitsspeicher aufräumen
    public void Destroy() {
        _windowThread.destroy();

        for (Renderer renderer : _renderers) {
            renderer.destroy();
        }

        _renderers.clear();
        _destroyed = true;
    }
}
