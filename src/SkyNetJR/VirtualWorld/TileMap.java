package SkyNetJR.VirtualWorld;

import SkyNetJR.Settings;
import SkyNetJR.Utils.ValueNoise2D;

import java.io.*;
import java.util.Random;

public class TileMap {
    private static final String SIGNATURE = "SkyNetJR.VirtualWorld.TileMap";

    private int _width;
    private int _height;
    private int _tileSize;
    private GenerationInfo _generationInfo;
    private Tile[][] _tiles;
    private int _totalLandTiles;
    private long _mapTime;

    private Random random;

    public static TileType[] GetNeighbourTypes(Tile[][] t, int x, int y){
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

    public static Double[] GetNeighbourEnergy(Tile[][] t, int x, int y){
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
        _mapTime = 0;
    }

    public double RequestConsumeEnergy(Tile t, double energy){
        if (_tiles[t.X][t.Y].Energy >= energy){
            _tiles[t.X][t.Y].Energy -= energy;
        }else {
            energy = _tiles[t.X][t.Y].Energy;
            _tiles[t.X][t.Y].Energy = 0d;
        }

        return energy;
    }

    public void SetDefaults() {
        _width = Settings.WorldSettings.Width;
        _height = Settings.WorldSettings.Height;
        _tileSize = Settings.WorldSettings.TileSize;
        _generationInfo = GenerationInfo.GetDefaults();
        _totalLandTiles = 0;
    }

    public void Generate() {
        ValueNoise2D vn = new ValueNoise2D(_width, _height, _generationInfo);
        vn.Calculate();
        double[][] heightMap = vn.getHeightMap();

        if (_height > 0 && _width > 0 && _tileSize > 0 && _generationInfo != null) {
            _tiles = new Tile[_width][_height];

            for (int x = 0; x < _width; x++) {
                for (int y = 0; y < _height; y++) {
                    _tiles[x][y] = new Tile(Settings.SimulationSettings.StartEnergy, heightMap[x][y] <= _generationInfo.LandThreshold ? TileType.Land : TileType.Water, x, y);
                    if (_tiles[x][y].getType() == TileType.Land) _totalLandTiles++;
                }
            }
        }
    }

    public void Update(long deltaTime) {Update(deltaTime, 0, 1);}
    public void Update(long deltaTime, int begin, int slice) {
        _mapTime += deltaTime;

        for (int x = begin; x < _width; x += slice) {
            for (int y = 0; y < _height; y++) {
                if (_tiles[x][y].getType() != TileType.Water && _tiles[x][y].Energy != Settings.SimulationSettings.MaxEnergyPerTile) {
                    double influence = Settings.SimulationSettings.BaseInfluence;

                    TileType[] nT = GetNeighbourTypes(_tiles, x, y);
                    Double[] nE = GetNeighbourEnergy(_tiles, x, y);

                    for (int i = 0; i < nT.length; i++) {
                        if (nT[i] == TileType.Water) influence += Settings.SimulationSettings.WaterInfluence;
                        else if (nT[i] == TileType.Land && nE[i] >= Settings.SimulationSettings.MaxEnergyPerTile * Settings.SimulationSettings.TileInfluenceThreshold) {
                            influence += nE[i] / Settings.SimulationSettings.MaxEnergyPerTile * Settings.SimulationSettings.OutGrownTileInfluence;
                        }
                    }

                    _tiles[x][y].Energy += Settings.SimulationSettings.BaseEnergyGeneration * influence * ((double) deltaTime / 1000d);

                    if (random.nextDouble() <= Settings.SimulationSettings.RandomEnergyGenerationChance)
                        _tiles[x][y].Energy += Settings.SimulationSettings.RandomEnergyGeneration;

                    if (_tiles[x][y].Energy > Settings.SimulationSettings.MaxEnergyPerTile)
                        _tiles[x][y].Energy = (double)Settings.SimulationSettings.MaxEnergyPerTile;

                    else if (_tiles[x][y].Energy < 0)
                        _tiles[x][y].Energy = 0.0d;
                }
            }
        }
    }

    public Tile[][] getTiles() {
        return _tiles;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public int getTileSize() {
        return _tileSize;
    }

    public GenerationInfo getGenerationInfo() {
        return _generationInfo;
    }

    public void setGenerationInfo(GenerationInfo generationInfo) {
        this._generationInfo = generationInfo;
    }

    public int getTotalLandTiles() {
        return _totalLandTiles;
    }

    public void saveToFile(String fileName) throws IOException {
        FileOutputStream of = new FileOutputStream(fileName, false);
        ObjectOutputStream oos = new ObjectOutputStream(of);
        oos.writeObject(this);

        oos.flush();
        of.flush();

        oos.close();
        of.close();
    }

    public static TileMap LoadFromFile(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream _if = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(_if);

        TileMap t = (TileMap)ois.readObject();

        ois.close();
        _if.close();

        return t;
    }

    public long getMapTime() {
        return _mapTime;
    }
}
