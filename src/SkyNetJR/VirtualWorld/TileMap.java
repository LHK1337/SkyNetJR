package SkyNetJR.VirtualWorld;

import SkyNetJR.Settings;
import SkyNetJR.Utils.ValueNoise2D;

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

    public TileMap() {
        readyTilesNextSwapLock = new Object();
    }

    public void SetDefaults() {
        width = Settings.WorldSettings.Width;
        height = Settings.WorldSettings.Height;
        tileSize = Settings.WorldSettings.TileSize;
        generationInfo = GenerationInfo.GetDefaults();
        MaxEnergyPerTile = Settings.WorldSettings.MaxEnergyPerTile;
        BaseEnergyGeneration = Settings.WorldSettings.BaseEnergyGeneration;
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
                    Tiles[0][x][y] = new Tile(Settings.WorldSettings.StartEnergy, heightMap[x][y] <= generationInfo.LandThreshold ? TileType.Land : TileType.Water);
                    Tiles[1][x][y] = new Tile(Settings.WorldSettings.StartEnergy, heightMap[x][y] <= generationInfo.LandThreshold ? TileType.Land : TileType.Water);
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

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Tiles[readFromTileMapIndex][x][y].getType() != TileType.Water) {
                    double influence = Settings.SimulationSettings.BaseInfluence;

                    if (x > 0 && Tiles[readFromTileMapIndex][x - 1][y].getType() == TileType.Water)
                        influence += Settings.SimulationSettings.WaterInfluence;
                    else if (x > 0 && Tiles[readFromTileMapIndex][x - 1][y].getType() == TileType.Land && Tiles[readFromTileMapIndex][x - 1][y].Energy >= MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                        influence += Tiles[readFromTileMapIndex][x - 1][y].Energy / MaxEnergyPerTile * Settings.SimulationSettings.MaxGrownTileInfluence;
                    }

                    if (y > 0 && Tiles[readFromTileMapIndex][x][y - 1].getType() == TileType.Water)
                        influence += Settings.SimulationSettings.WaterInfluence;
                    else if (y > 0 && Tiles[readFromTileMapIndex][x][y - 1].getType() == TileType.Land && Tiles[readFromTileMapIndex][x][y - 1].Energy >= MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                        influence += Tiles[readFromTileMapIndex][x][y - 1].Energy / MaxEnergyPerTile * Settings.SimulationSettings.MaxGrownTileInfluence;
                    }

                    if (x < width - 1 && Tiles[readFromTileMapIndex][x + 1][y].getType() == TileType.Water)
                        influence += Settings.SimulationSettings.WaterInfluence;
                    else if (x < width - 1 && Tiles[readFromTileMapIndex][x + 1][y].getType() == TileType.Land && Tiles[readFromTileMapIndex][x + 1][y].Energy >= MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                        influence += Tiles[readFromTileMapIndex][x + 1][y].Energy / MaxEnergyPerTile * Settings.SimulationSettings.MaxGrownTileInfluence;
                    }

                    if (y < height - 1 && Tiles[readFromTileMapIndex][x][y + 1].getType() == TileType.Water)
                        influence += Settings.SimulationSettings.WaterInfluence;
                    else if (y < height - 1 && Tiles[readFromTileMapIndex][x][y + 1].getType() == TileType.Land && Tiles[readFromTileMapIndex][x][y + 1].Energy >= MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                        influence += Tiles[readFromTileMapIndex][x][y + 1].Energy / MaxEnergyPerTile * Settings.SimulationSettings.MaxGrownTileInfluence;
                    }

                    Tiles[writeToTileMapIndex][x][y].Energy = Tiles[readFromTileMapIndex][x][y].Energy + BaseEnergyGeneration * influence * ((double) deltaTime / 1000d);

                    if (Tiles[writeToTileMapIndex][x][y].Energy > MaxEnergyPerTile)
                        Tiles[writeToTileMapIndex][x][y].Energy = MaxEnergyPerTile;
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
