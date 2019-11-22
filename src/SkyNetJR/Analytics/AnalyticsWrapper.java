package SkyNetJR.Analytics;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Threading.WindowThread;
import SkyNetJR.VirtualWorld.VirtualWorld;

public class AnalyticsWrapper {
    private VirtualWorld _virtualWorld;
    private Population _population;
    private WindowThread _windowThread;

    public AnalyticsWrapper(VirtualWorld virtualWorld, Population population, WindowThread windowThread) {
        _virtualWorld = virtualWorld;
        _population = population;
        _windowThread = windowThread;
    }

    public double getFrameRenderingTime() { return _windowThread.getRenderTime(); }
    public double getFramePerSecond() { return 1 / getFrameRenderingTime(); }

    public double getWorldSimulationTime() { return _virtualWorld.getLastSimulationTime(); }
    public double getWorldSimulationsPerSecond() { return 1 / getWorldSimulationTime(); }

    public double getPopulationSimulationTime() { return _population.getLastSimulationTime(); }
    public double getPopulationSimulationsPerSecond() { return 1 / getWorldSimulationTime(); }
}
