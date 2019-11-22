package SkyNetJR.Creatures;

import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//Todo: SimulationThread Loop
//* SimulationThread Loop
//? Acquire TileMap
// Creature sensing
// Creature thinking
// Reset CollisionGrid
// Creature acting

public class Population {
    private VirtualWorld World;

    private List[][] CollisionGrid;

    private Map<Tile, Double> UnstagedTileEnergies;

    private final Object CreatureLock = new Object();
    private List<Creature> Creatures;

    private long LastSimulationTime;

    private int timePrecision;
    private boolean isRunning;
    private boolean realTime;

    public int getTimePrecision() {
        return timePrecision;
    }
    public void setTimePrecision(int timePrecision) {
        this.timePrecision = timePrecision;
    }

    public boolean isRunning() {
        return isRunning;
    }
    public void setRunning(boolean running) {
        isRunning = running;

        if (running) {
            synchronized (populationSimulationThread.StopLock) {
                populationSimulationThread.StopLock.notify();
            }

            if (populationSimulationThread.getState() == Thread.State.NEW)
                populationSimulationThread.start();
        }
    }

    public boolean isRealTime() {
        return realTime;
    }
    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    private PopulationSimulationThread populationSimulationThread;

    private Population(){
        UnstagedTileEnergies = new ConcurrentHashMap<>();
        Creatures = new ArrayList<>();
        realTime = true;

        //TODO ctm change
        populationSimulationThread = new PopulationSimulationThread(this, CreatureThinkingMethod.CpuSingleThread);
    }

    public Population(VirtualWorld world, int timePrecision) {
        this();

        this.timePrecision = timePrecision;
        World = world;
        CollisionGrid = new List[(world.getTileMap().getWidth() * world.getTileMap().getTileSize() / Settings.CreatureSettings.CreatureSize) + 1][(world.getTileMap().getHeight() * world.getTileMap().getTileSize() / Settings.CreatureSettings.CreatureSize) + 1];
        for (int x = 0; x < CollisionGrid.length; x++) {
            Arrays.fill(CollisionGrid[x], new ArrayList());
        }
    }

    public void ClearCollisionGrid() {
        for (List[] lists : CollisionGrid) {
            for (List list : lists) {
                list.clear();
            }
        }
    }

    public void UpdateCollisionGrid(Creature c, int x, int y) {
        int gridX = x / Settings.CreatureSettings.CreatureSize;
        int gridY = y / Settings.CreatureSettings.CreatureSize;

        if (gridX > 0 && gridY > 0 && gridX < CollisionGrid.length && gridY < CollisionGrid[gridX].length)
            CollisionGrid[gridX][gridY].add(c);
    }

    public Tile getTile(int positionX, int positionY) {
        int tileX = positionX / World.getTileMap().getTileSize();
        int tileY = positionY / World.getTileMap().getTileSize();

        if (tileX < 0 || tileY < 0 || tileX >= World.getTileMap().getWidth() || tileY >= World.getTileMap().getHeight())
            return Tile.Void;

        return World.getTileMap().getReadyTiles()[tileX][tileY];
    }

    public Creature getCollidingCreature(int positionX, int positionY) {
        if (positionX < 0 || positionY < 0 || positionX > World.getTileMap().getWidth() * World.getTileMap().getTileSize() || positionY > World.getTileMap().getHeight() * World.getTileMap().getTileSize())
            return null;

        List cs = CollisionGrid[positionX / Settings.CreatureSettings.CreatureSize][positionY / Settings.CreatureSettings.CreatureSize];

        if (cs == null || cs.size() == 0) return null;
        else {
            return (Creature) cs.get(new Random().nextInt(cs.size()));
        }
    }

    public double Eat(int positionX, int positionY, double value) {
        Tile t = getTile(positionX, positionY);

        if (t == null || t.getType() == TileType.Water || t == Tile.Void) return 0d;

        return World.getTileMap().RequestConsumeEnergy(t, value);

        //TODO remove
//        double maxE = 0;
//
//        if (UnstagedTileEnergies.containsKey(t)) {
//            maxE = UnstagedTileEnergies.get(t);
//        } else maxE = t.Energy;
//
//        if (value > maxE){
//            value = maxE;
//        }
//
//        if (value != 0) {
//            World.getTileMap().EnqueueEnergyChange(positionX / World.getTileMap().getTileSize(), positionY / World.getTileMap().getTileSize(), -value, UnstagedTileEnergies, t);
//
//            UnstagedTileEnergies.put(t, maxE - value);
//        }
//        return value;
    }

    public void FillPopulation() {
        Random r = new Random();

        int count = Settings.CreatureSettings.InitialPopulationSize - Creatures.size();

        for (int i = 0; i < count; i++) {
            AddCreature(new Creature(
                    r.nextInt(World.getTileMap().getWidth()) * World.getTileMap().getTileSize() + Settings.CreatureSettings.CreatureSize / 2,
                    r.nextInt(World.getTileMap().getHeight()) * World.getTileMap().getTileSize() + Settings.CreatureSettings.CreatureSize / 2,
                    this));
        }
    }

    public void RemoveCreature(Creature creature) {
        synchronized (CreatureLock){
            Creatures.remove(creature);
        }
    }

    public void AddCreature(Creature creature){
        synchronized (CreatureLock){
            Creatures.add(creature);
        }
    }

    public List<Creature> getCreatures(){
        return Creatures;
    }

    public void Destroy() {
        throw new NotImplementedException();
    }

    public long getLastSimulationTime(){
        return LastSimulationTime;
    }

    public void setLastSimulationTime(long time){
        LastSimulationTime = time;
    }

    public void ClearUnstagedEnergies() {
        UnstagedTileEnergies.clear();
    }
}
