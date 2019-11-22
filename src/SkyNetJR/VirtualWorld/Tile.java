package SkyNetJR.VirtualWorld;

public class Tile {
    public static final Tile Void = new Tile(0, TileType.Water, -1, -1);

    public Double Energy;
    private TileType Type;

    public int X;
    public int Y;

    public Tile(double energy, TileType type, int x, int y) {
        Energy = energy;
        Type = type;

        X = x;
        Y = y;
    }

    public TileType getType() {
        return Type;
    }
}
