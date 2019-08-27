package SkyNetJR.VirtualWorld;

public class Tile {
    public Double Energy;
    private TileType Type;

    public Tile(double energy, TileType type){
        Energy = energy;
        Type = type;
    }

    public TileType getType() {
        return Type;
    }
}
