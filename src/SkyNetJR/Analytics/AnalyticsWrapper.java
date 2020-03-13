/*
* Stellt eine Schnittstelle zur Verfügung, um die Daten aus der Simulation für die Diagramme auszuwerten.
* */

package SkyNetJR.Analytics;

import SkyNetJR.Creatures.Creature;
import SkyNetJR.Creatures.Population;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.util.Arrays;

public class AnalyticsWrapper {
    private VirtualWorld _virtualWorld;
    private Population _population;

    public AnalyticsWrapper(VirtualWorld virtualWorld, Population population) {
        _virtualWorld = virtualWorld;
        _population = population;
    }

    // Reale Zeit, die zur Berechnung einer Zeiteinheit der Welt der Simulation, benötigt wird.
    public double getWorldSimulationTime() { return _virtualWorld.getLastSimulationTime(); }
    public double getWorldSimulationsPerSecond() { return 1 / getWorldSimulationTime(); }

    // Reale Zeit, die zur Berechnung einer Zeiteinheit der Population der Simulation, benötigt wird.
    public double getPopulationSimulationTime() { return _population.getLastSimulationTime(); }
    public double getPopulationSimulationsPerSecond() { return 1 / getPopulationSimulationTime(); }

    // Umfang der Population
    public int getPopulationSize() { return _population.getCreatures().size(); }
    // Ermittelt aktuell bestmöglich angepasste Kreatur
    public Creature getBestCreature() { if (getPopulationSize() >= 1) return _population.getCreatures().get(0); else return null; }

    // Akkumulation der Energien in der Simulation
    public Double getTotalMapEnergy() {
        return Arrays.stream(_virtualWorld.getTileMap().getTiles()).flatMap(Arrays::stream).mapToDouble(t -> t.Energy).sum();
    }
    public Double getTotalCreatureEnergy() {
        return _population.getCreatures().stream().mapToDouble(Creature::getEnergy).sum();
    }
    public Double getTotalWorldEnergy() { return getTotalCreatureEnergy() + getTotalMapEnergy(); }
}
