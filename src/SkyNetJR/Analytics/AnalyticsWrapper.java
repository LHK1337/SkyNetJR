package SkyNetJR.Analytics;

import SkyNetJR.Creatures.Creature;
import SkyNetJR.Creatures.Population;
import SkyNetJR.Threading.WindowThread;
import SkyNetJR.VirtualWorld.Tile;
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

    public int getPopulationSize() { return _population.getCreatures().size(); }
    public Creature getBestCreature() { if (getPopulationSize() >= 1) return _population.getCreatures().get(0); else return null; }

    public Double getTotalMapEnergy() {
        double totalEnergy = 0;

        for (Tile[] tt : _virtualWorld.getTileMap().getTiles())
            for (Tile t: tt){
                totalEnergy += t.Energy;
            }

        return totalEnergy;
    }
    public Double getTotalCreatureEnergy() {
        double totalEnergy = 0;

        for (Creature c : _population.getCreatures())
            totalEnergy += c.getEnergy();

        return totalEnergy;
    }
    public Double getTotalWorldEnergy() { return getTotalCreatureEnergy() + getTotalMapEnergy(); }
}
