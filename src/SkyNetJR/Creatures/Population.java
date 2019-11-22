package SkyNetJR.Creatures;

import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Population {
    private VirtualWorld _world;

    private List[][] _collisionGrid;

    private final Object _creatureLock = new Object();
    private List<Creature> _creatures;

    private long _lastSimulationTime;
    private boolean _isRunning;
    private boolean _realTime;


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
    /*package-private*/ Creature getCollidingCreature(int positionX, int positionY) {
        if (
                positionX < 0 || positionY < 0 ||
                        positionX > _world.getTileMap().getWidth() * _world.getTileMap().getTileSize() ||
                        positionY > _world.getTileMap().getHeight() * _world.getTileMap().getTileSize()
        )
            return null;

        List cs = _collisionGrid[positionX / Settings.CreatureSettings.CreatureSize][positionY / Settings.CreatureSettings.CreatureSize];

        if (cs == null || cs.size() == 0) return null;
        else {
            return (Creature) cs.get(new Random().nextInt(cs.size()));  // return random colliding creature
        }
    }

    public void setLastSimulationTime(long time){ _lastSimulationTime = time; }
    public void setRealTime(boolean realTime) { this._realTime = realTime; }
    public void setRunning(boolean running) { _isRunning = running; }
    /*package-private*/ void UpdateCollisionGrid(Creature c, int x, int y) {
        int gridX = x / Settings.CreatureSettings.CreatureSize;
        int gridY = y / Settings.CreatureSettings.CreatureSize;

        if (gridX > 0 && gridY > 0 && gridX < _collisionGrid.length && gridY < _collisionGrid[gridX].length)
            //noinspection unchecked    <-- IntelliJ IDEA specific
            _collisionGrid[gridX][gridY].add(c);
    }
    /*package-private*/ double Eat(int positionX, int positionY, double value) {
        Tile t = getTile(positionX, positionY);

        if (t == null || t.getType() == TileType.Water || t == Tile.Void) return 0d;

        return _world.getTileMap().RequestConsumeEnergy(t, value);
    }


    private Population(){
        _creatures = new ArrayList<>();
        _realTime = true;
    }

    public Population(VirtualWorld world) {
        this();

        _world = world;
        _collisionGrid =
                new List
                        [(world.getTileMap().getWidth() * world.getTileMap().getTileSize() / Settings.CreatureSettings.CreatureSize) + 1]
                        [(world.getTileMap().getHeight() * world.getTileMap().getTileSize() / Settings.CreatureSettings.CreatureSize) + 1];
        for (List[] lists : _collisionGrid) {
            Arrays.fill(lists, new ArrayList());
        }
    }

    public void FillPopulation() {
        Random r = new Random();

        int count = Settings.CreatureSettings.InitialPopulationSize - _creatures.size();

        for (int i = 0; i < count; i++) {
            AddCreature(new Creature(
                    r.nextInt(_world.getTileMap().getWidth()) * _world.getTileMap().getTileSize() + Settings.CreatureSettings.CreatureSize / 2,
                    r.nextInt(_world.getTileMap().getHeight()) * _world.getTileMap().getTileSize() + Settings.CreatureSettings.CreatureSize / 2,
                    this));
        }
    }

    public void RemoveCreature(Creature creature) {
        synchronized (_creatureLock){
            _creatures.remove(creature);
        }
    }

    public void AddCreature(Creature creature){
        synchronized (_creatureLock){
            _creatures.add(creature);
        }
    }

    public void Destroy() {
        _creatures.clear();
    }

    public void Update(long deltaTime, boolean multiThread, ExecutorService threadPool){

        Stack<Future> futures = null;
        if (multiThread) futures = new Stack<>();

        for (Creature c : _creatures)
        {
            c.Sense();                  // sense environment

            if (!multiThread || threadPool == null)
                c.getBrain().EvaluateCpu(); // think.. (single threaded)
            else {
                futures.push(threadPool.submit(() -> c.getBrain().EvaluateCpu())); // think.. (multi threaded)
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

        ClearCollisionGrid();

        for (int i = 0; i < _creatures.size(); i++) {
            Creature c = _creatures.get(i);
            c.Act(deltaTime);           // react on environment
            if (c.isDestroyed()) i--;
        }

        // create new Creation when Population is too small
        if (_creatures.size() < Settings.CreatureSettings.MinPopulationSize)
            FillPopulation();
    }

    private void ClearCollisionGrid() {
        for (List[] lists : _collisionGrid)
            for (List list : lists)
                list.clear();
    }
}
