package SkyNetJR.VirtualWorld;

import SkyNetJR.Settings;
import SkyNetJR.Utils.ValueNoise2D;
import org.lwjglx.debug.org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.Queue;
import java.util.Random;

public class TileMap {
    private final Object readyTilesNextSwapLock;
    private int width;
    private int height;
    private int tileSize;
    private GenerationInfo generationInfo;
    private double MaxEnergyPerTile;
    private double BaseEnergyGeneration;
    private Tile[][][] Tiles;
    private int readyTilesIndex;
    private int readyTilesUseCount;
    private int latestTileMapIndex = 0;
    private Queue<EnergyChange> EnergyChanges = new BlockingArrayQueue<>();

    private Random random;

    public static TileType[] getNeighbourTypes(Tile[][] t, int x, int y){
        TileType[] neighbours = new TileType[4];

        if (x > 0)
        {
            neighbours[0] = t[x - 1][y].getType() == TileType.Water ? TileType.Water : TileType.Land;
        } else neighbours[0] = null;

        if (y > 0)
        {
            neighbours[1] = t[x][y - 1].getType() == TileType.Water ? TileType.Water : TileType.Land;
        } else neighbours[1] = null;

        if (x < t.length - 1)
        {
            neighbours[2] = t[x + 1][y].getType() == TileType.Water ? TileType.Water : TileType.Land;
        } else neighbours[2] = null;

        if (y < t[x].length - 1)
        {
            neighbours[3] = t[x][y + 1].getType() == TileType.Water ? TileType.Water : TileType.Land;
        } else neighbours[3] = null;

        return neighbours;
    }

    public static Double[] getNeighbourEnergy(Tile[][] t, int x, int y){
        Double[] neighbours = new Double[4];

        if (x > 0)
        {
            neighbours[0] = t[x - 1][y].Energy;
        } else neighbours[0] = null;

        if (y > 0)
        {
            neighbours[1] = t[x][y - 1].Energy;
        } else neighbours[1] = null;

        if (x < t.length - 1)
        {
            neighbours[2] = t[x + 1][y].Energy;
        } else neighbours[2] = null;

        if (y < t[x].length - 1)
        {
            neighbours[3] = t[x][y + 1].Energy;
        } else neighbours[3] = null;

        return neighbours;
    }

    public TileMap() {
        random = new Random();
        readyTilesNextSwapLock = new Object();
    }

    public void SetDefaults() {
        width = Settings.WorldSettings.Width;
        height = Settings.WorldSettings.Height;
        tileSize = Settings.WorldSettings.TileSize;
        generationInfo = GenerationInfo.GetDefaults();
        MaxEnergyPerTile = Settings.SimulationSettings.MaxEnergyPerTile;
        BaseEnergyGeneration = Settings.SimulationSettings.BaseEnergyGeneration;
    }

    public void Generate() {
        ValueNoise2D vn = new ValueNoise2D(width, height, generationInfo);
        vn.Calculate();
        double[][] heightMap = vn.getHeightMap();

        if (height > 0 && width > 0 && tileSize > 0 && generationInfo != null) {
            Tiles = new Tile[2][width][height];
            readyTilesIndex = 0;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Tiles[0][x][y] = new Tile(Settings.SimulationSettings.StartEnergy, heightMap[x][y] <= generationInfo.LandThreshold ? TileType.Land : TileType.Water);
                    Tiles[1][x][y] = new Tile(Settings.SimulationSettings.StartEnergy, heightMap[x][y] <= generationInfo.LandThreshold ? TileType.Land : TileType.Water);
                }
            }
        }
    }

    public void Update(int deltaTime) {
        int readFromTileMapIndex;
        int writeToTileMapIndex;

        if (readyTilesIndex == latestTileMapIndex) {
            readFromTileMapIndex = readyTilesIndex;
            writeToTileMapIndex = 1 - readyTilesIndex;
        } else {
            writeToTileMapIndex = readFromTileMapIndex = latestTileMapIndex;
        }

        while (!EnergyChanges.isEmpty()) {
            EnergyChange ec = EnergyChanges.poll();

            double te = Tiles[readFromTileMapIndex][ec.X][ec.Y].Energy;

            te += ec.Energy;

            if (te < 0) {
                //TODO: Fix
                //! WARNING MUTED
                //System.out.println("[WARNING] Enqueued EnergyChange drains more Energy than possible.");
                Tiles[readFromTileMapIndex][ec.X][ec.Y].Energy = 0d;
            }else Tiles[readFromTileMapIndex][ec.X][ec.Y].Energy = te;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Tiles[readFromTileMapIndex][x][y].getType() != TileType.Water && Tiles[readFromTileMapIndex][x][y].Energy != MaxEnergyPerTile) {
                    double influence = Settings.SimulationSettings.BaseInfluence;

                    TileType[] nT = getNeighbourTypes(Tiles[readFromTileMapIndex], x, y);
                    Double[] nE = getNeighbourEnergy(Tiles[readFromTileMapIndex], x, y);

                    for (int i = 0; i < nT.length; i++) {
                        if (nT[i] == TileType.Water) influence += Settings.SimulationSettings.WaterInfluence;
                        else if (nT[i] == TileType.Land && nE[i] >= MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                            influence += nE[i] / MaxEnergyPerTile * Settings.SimulationSettings.OutGrownTileInfluence;
                        }
                    }

                    Tiles[writeToTileMapIndex][x][y].Energy = Tiles[readFromTileMapIndex][x][y].Energy + BaseEnergyGeneration * influence * ((double) deltaTime / 1000d);

                    if (random.nextDouble() <= Settings.SimulationSettings.RandomEnergyGenerationChance)
                        Tiles[writeToTileMapIndex][x][y].Energy += Settings.SimulationSettings.RandomEnergyGeneration;

                    if (Tiles[writeToTileMapIndex][x][y].Energy > MaxEnergyPerTile)
                        Tiles[writeToTileMapIndex][x][y].Energy = MaxEnergyPerTile;

                    else if (Tiles[writeToTileMapIndex][x][y].Energy < 0)
                        Tiles[writeToTileMapIndex][x][y].Energy = 0.0d;
                }
            }
        }

        latestTileMapIndex = writeToTileMapIndex;

        if (readyTilesUseCount == 0)
            SwapReadyTiles();
    }

    public void AcquireUse() {
        readyTilesUseCount++;
    }

    public void ReleaseUse() {
        readyTilesUseCount--;
    }

    private void SwapReadyTiles() {
        readyTilesIndex = 1 - readyTilesIndex;
        synchronized (readyTilesNextSwapLock) {
            readyTilesNextSwapLock.notifyAll();
        }
    }

    public void WaitForNextSwap() throws InterruptedException {
        synchronized (readyTilesNextSwapLock) {
            readyTilesNextSwapLock.wait();
        }
    }

    public Tile[][] getReadyTiles() {
        return Tiles[readyTilesIndex];
    }

    public void EnqueueEnergyChange(int x, int y, double energy){
        EnergyChanges.add(new EnergyChange(x, y, energy));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }

    public GenerationInfo getGenerationInfo() {
        return generationInfo;
    }

    public void setGenerationInfo(GenerationInfo generationInfo) {
        this.generationInfo = generationInfo;
    }

    public double getMaxEnergyPerTile() {
        return MaxEnergyPerTile;
    }

    public void setMaxEnergyPerTile(double maxEnergyPerTile) {
        MaxEnergyPerTile = maxEnergyPerTile;
    }

    public double getBaseEnergyGeneration() {
        return BaseEnergyGeneration;
    }

    public void setBaseEnergyGeneration(double baseEnergyGeneration) {
        BaseEnergyGeneration = baseEnergyGeneration;
    }
}
