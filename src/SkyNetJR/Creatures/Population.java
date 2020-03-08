package SkyNetJR.Creatures;

import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Population {
    private static final String SIGNATURE = "SkyNetJR.Creatures.Population";

    private VirtualWorld _world;

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

    public void setLastSimulationTime(long time){ _lastSimulationTime = time; }
    public void setRealTime(boolean realTime) { this._realTime = realTime; }
    public void setRunning(boolean running) { _isRunning = running; }

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
    }

    public void FillPopulation() {
        Random r = new Random();

        TileMap map = _world.getTileMap();
        double pSpawnCreature = (Settings.CreatureSettings.InitialPopulationSizeTarget - _creatures.size()) / (double)map.getTotalLandTiles();

        for (Tile[] tt : map.getTiles())
            for (Tile t : tt){
                if (t.getType() == TileType.Land && r.nextDouble() < pSpawnCreature)
                    AddCreature(new Creature(t.X * map.getTileSize() + map.getTileSize() / 2d,
                                             t.Y * map.getTileSize() + map.getTileSize() / 2d,
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

        for (int i = 0; i < _creatures.size(); i++) {
            Creature c = _creatures.get(i);
            c.Act(deltaTime);           // react on environment
            if (c.isDestroyed()) i--;
        }

        // create new Creation when Population is too small
        if (_creatures.size() < Settings.CreatureSettings.MinPopulationSize)
            FillPopulation();
    }

    public void saveToFile(String fileName) throws IOException {
        FileOutputStream of = new FileOutputStream(fileName, false);

        of.write(SIGNATURE.getBytes(StandardCharsets.US_ASCII));

        synchronized (_creatureLock){
            of.write(ByteBuffer.wrap(new byte[Integer.BYTES]).putInt(_creatures.size()).array());

            for (Creature c : _creatures){
                if (c.getEnergy() <= 0)
                    continue;

                byte[] cBytes = c.serialize();
                of.write(ByteBuffer.wrap(new byte[Integer.BYTES]).putInt(cBytes.length).array());
                of.write(c.serialize());
            }
        }

        of.flush();
        of.close();
    }

    public static Population LoadFromFile(String fileName, VirtualWorld world) throws IOException {
        FileInputStream _if = new FileInputStream(fileName);

        byte[] sigBytes = new byte[SIGNATURE.length()];
        if (_if.read(sigBytes) != sigBytes.length || new String(sigBytes) == SIGNATURE)
            throw new IOException("File Signature mismatch");

        Population p = new Population(world);

        ByteBuffer intBuffer = ByteBuffer.wrap(new byte[Integer.BYTES]);

        if (_if.read(intBuffer.array()) != intBuffer.array().length)
            throw new IOException("Corrupted creature count");

        int creatureCount = intBuffer.getInt();

        for (int i = 0; i < creatureCount; i++) {
            try {
                intBuffer = ByteBuffer.wrap(intBuffer.array()); // reset buffer

                if (_if.read(intBuffer.array()) != intBuffer.array().length)
                    throw new IOException("Corrupted creature length");

                int creatureBytesLength = intBuffer.getInt();
                byte[] creatureBytes = new byte[creatureBytesLength];
                if (_if.read(creatureBytes) != creatureBytesLength)
                    throw new IOException("Corrupted creature date");

                p._creatures.add(Creature.Deserialize(creatureBytes, p));

            } catch (IOException e){
                e.printStackTrace(System.err);

                if (p._creatures.size() > 0)
                {
                    System.out.println("Continuing with already loaded creatures..");
                    break;
                }
            }
        }

        return p;
    }
}
