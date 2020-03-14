/*
* Enthält Logik der Population der Simulation
* */

package SkyNetJR.Creatures;

import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Population {
    // Referenz zur aktuellen Welt
    private VirtualWorld _world;

    // Auflistung aller Kreaturen in der Population
    private final Object _creatureLock = new Object();
    private List<Creature> _creatures;

    // Eigenschaften der Population
    private long _lastSimulationTime;
    private boolean _isRunning;
    private boolean _realTime;

    /*package-private*/ double Eat(int positionX, int positionY, double value) {
        Tile t = getTile(positionX, positionY);

        if (t == null || t.getType() == TileType.Water || t == Tile.Void) return 0d;

        return _world.getTileMap().requestConsumeEnergy(t, value);
    }

    private Population(){
        _creatures = new ArrayList<>();
        _realTime = true;
    }
    public Population(VirtualWorld world) {
        this();

        _world = world;
    }

    // Population mit neuen Kreaturen füllen
    public void FillPopulation() {
        _isRunning = true;
        Random r = new Random();

        TileMap map = _world.getTileMap();
        // Wahrscheinlichkeit eine Kreatur auf einem Feld zu erschaffen (berechnet sich aus dem gesetzten Populationsumfangsziel und der aktuellen Anzahl an Kreaturen)
        double pSpawnCreature = (Settings.CreatureSettings.InitialPopulationSizeTarget - _creatures.size()) / (double)map.getTotalLandTiles();

        // Kreaturen nur auf Land erschaffen
        for (Tile[] tt : map.getTiles())
            for (Tile t : tt){
                if (t.getType() == TileType.Land && r.nextDouble() < pSpawnCreature)
                    AddCreature(new Creature(t.X * map.getTileSize() + map.getTileSize() / 2d,
                                             t.Y * map.getTileSize() + map.getTileSize() / 2d,
                                            this));
            }
    }

    // Kreatur aus Population entfernen
    public void RemoveCreature(Creature creature) {
        synchronized (_creatureLock){
            _creatures.remove(creature);
        }
    }

    // Kreatur zur Population hinzufügen
    public void AddCreature(Creature creature){
        synchronized (_creatureLock){
            _creatures.add(creature);
        }
    }

    // Funktion zum berechnen der nächsten Zeiteinheit der Population der Simulation
    public void Update(long deltaTime, boolean multiThread, ExecutorService threadPool){
        Stack<Future> futures = null;
        if (multiThread) futures = new Stack<>();

        for (Creature c : _creatures)
        {
            c.sense();

            if (!multiThread || threadPool == null)
                c.getBrain().evaluate();
            else {
                futures.push(threadPool.submit(() -> c.getBrain().evaluate())); // think.. (multi threaded)
            }
        }

        if (multiThread){
            while (!futures.empty()) {
                try {
                    futures.pop().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < _creatures.size(); i++) {
            Creature c = _creatures.get(i);
            c.act(deltaTime);
            if (c.isDestroyed()) i--;
        }

        // Neue Kreaturen erschaffen, falls Population droht auszusterben
        if (_creatures.size() < Settings.CreatureSettings.MinPopulationSize)
            FillPopulation();
    }

    // Speichern und Laden der Population zur und von der Festplatte
    public void saveToFile(String fileName) throws IOException {
        FileOutputStream of = new FileOutputStream(fileName, false);
        ObjectOutputStream oos = new ObjectOutputStream(of);
        oos.writeObject(this);

        oos.flush();
        of.flush();

        oos.close();
        of.close();
    }
    public static Population LoadFromFile(String fileName, VirtualWorld world) throws IOException, ClassNotFoundException {
        FileInputStream _if = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(_if);

        Population p = (Population)ois.readObject();

        ois.close();
        _if.close();

        return p;
    }

    // Getter und Setter
    public List<Creature> getCreatures(){
        return _creatures;
    }
    public long getLastSimulationTime(){
        return _lastSimulationTime;
    }
    public boolean isRunning() {
        return _isRunning;
    }
    public boolean isRealTime() {
        return _realTime;
    }

    /*package-private*/ Tile getTile(int positionX, int positionY) {
        int tileX = positionX / _world.getTileMap().getTileSize();
        int tileY = positionY / _world.getTileMap().getTileSize();

        if (tileX < 0 || tileY < 0 || tileX >= _world.getTileMap().getWidth() || tileY >= _world.getTileMap().getHeight())
            return Tile.Void;

        return _world.getTileMap().getTiles()[tileX][tileY];
    }

    public void setLastSimulationTime(long time){ _lastSimulationTime = time; }
    public void setRealTime(boolean realTime) { this._realTime = realTime; }
    public void setRunning(boolean running) { _isRunning = running; }

    // Zerstört Population und räumt Arbeitsspeicher auf
    public void Destroy() {
        _creatures.clear();
    }
}
