package SkyNetJR.Analytics;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.Rendering.RenderThread;
import SkyNetJR.VirtualWorld.VirtualWorld;

public class AnalyticsWrapper {
    private VirtualWorld _virtualWorld;
    private Population _population;
    private RenderThread _renderThread;

    public AnalyticsWrapper(VirtualWorld virtualWorld, Population population, RenderThread renderThread) {
        _virtualWorld = virtualWorld;
        _population = population;
        _renderThread = renderThread;
    }

    public double getFrameRenderingTime() { return _renderThread.getRenderTime(); }
    public double getFramePerSecond() { return 1 / getFrameRenderingTime(); }

    public double getWorldTimePrecision() { return _virtualWorld.getTimePrecision(); }
    public double getWorldSimulationTime() { return _virtualWorld.getLastSimulatedFrameTime(); }
    public double getWorldSimulationsPerSecond() { return 1 / getWorldSimulationTime(); }

    public double getPopulationTimePrecision() { return _population.getTimePrecision(); }
    public double getPopulationSimulationTime() { return _population.getLastSimulationTime(); }
    public double getPopulationSimulationsPerSecond() { return 1 / getWorldSimulationTime(); }
}
