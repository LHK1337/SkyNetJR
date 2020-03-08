package SkyNetJR.VirtualWorld;

import SkyNetJR.Settings;
import SkyNetJR.Utils.ValueNoise2D;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class TileMap {
    private static final String SIGNATURE = "SkyNetJR.VirtualWorld.TileMap";

    private int _width;
    private int _height;
    private int _tileSize;
    private GenerationInfo _generationInfo;
    private Tile[][] _tiles;
    private int _totalLandTiles;

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

        of.write(SIGNATURE.getBytes(StandardCharsets.US_ASCII));

        byte[] intBytes = new byte[Integer.BYTES * 3];
        ByteBuffer intBuffer = ByteBuffer.wrap(intBytes);
        intBuffer.putInt(_width);
        intBuffer.putInt(_height);
        intBuffer.putInt(_tileSize);
        of.write(intBytes);

        byte[] doubleBytes = new byte[_width * _height * Double.BYTES];
        ByteBuffer doubleBuffer = ByteBuffer.wrap(doubleBytes);
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                doubleBuffer.putDouble(x * _width + y, _tiles[x][y].getType() == TileType.Water ? -1 : _tiles[x][y].Energy);
            }
        }

        of.write(doubleBytes);

        of.flush();
        of.close();
    }

    public static TileMap LoadFromFile(String fileName) throws IOException {
        FileInputStream _if = new FileInputStream(fileName);

        byte[] sigBytes = new byte[SIGNATURE.length()];
        if (_if.read(sigBytes) != sigBytes.length || new String(sigBytes) == SIGNATURE)
            throw new IOException("File Signature mismatch");

        TileMap t = new TileMap();
        t._totalLandTiles = 0;

        byte[] intBytes = new byte[Integer.BYTES * 3];
        ByteBuffer intBuffer = ByteBuffer.wrap(intBytes);

        if (_if.read(intBytes) != intBytes.length)
            throw new IOException("Corrupted MapData Header");

        t._width = intBuffer.getInt();
        t._height = intBuffer.getInt();
        t._tileSize = intBuffer.getInt();

        byte[] mapData = new byte[t._width * t._height * Double.BYTES];
        if (_if.read(mapData) != mapData.length)
            throw new IOException("Corrupted MapData");

        t._tiles = new Tile[t._width][t._height];
        ByteBuffer buffer = ByteBuffer.wrap(mapData);

        for (int x = 0; x < t._width; x++) {
            for (int y = 0; y < t._height; y++) {
                double d = buffer.getDouble(x * t._width + y);

                if (d < 0) { t._tiles[x][y] = new Tile(0, TileType.Water, x, y); }
                else       { t._tiles[x][y] = new Tile(d, TileType.Land, x, y); t._totalLandTiles++; }
            }
        }

        t._generationInfo = null;

        _if.close();

        return t;
    }
}
