package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.Analytics.AnalyticsWrapper;
import SkyNetJR.Graphics.Rendering.Renderer;

public class AnalyticsRenderer extends Renderer {
    private AnalyticsWrapper _wrapper;

    public AnalyticsRenderer(AnalyticsWrapper wrapper) {
        _wrapper = wrapper;
    }

    @Override
    public void Render(int offsetX, int offsetY) {

    }

    @Override
    public void Destroy() {

    }
}
