/*
* Tile-Klasse
* Kleinste logische Einheit in der virtuellen Welt
* */

package SkyNetJR.VirtualWorld;

public class Tile {
    // Statisches Tile, welches universell für alle theorethischen Tiles außerhalb der virtuellen Welt steht. (Entspricht Ozean)
    public static final Tile Void = new Tile(0, TileType.Water, -1, -1);

    // Eigenschaften
    public Double Energy;
    private TileType Type;

    // Position
    public int X;
    public int Y;

    public Tile(double energy, TileType type, int x, int y) {
        Energy = energy;
        Type = type;

        X = x;
        Y = y;
    }

    // Getter
    public TileType getType() {
        return Type;
    }
}
