package SkyNetJR.VirtualWorld;

public class Tile {
    public static final Tile Void = new Tile(0, TileType.Water);

    public Double Energy;
    private TileType Type;

    public Tile(double energy, TileType type) {
        Energy = energy;
        Type = type;
    }

    public TileType getType() {
        return Type;
    }
}
