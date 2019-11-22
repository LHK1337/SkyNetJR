package SkyNetJR.VirtualWorld;

import java.util.Map;

public class EnergyChange {
    public final int X;
    public final int Y;
    public final double Energy;
    Map<Tile, Double> ParentUnstagedEntries;
    Tile Tile;

    public EnergyChange(int x, int y, double energy, Map<Tile, Double> unstagedEntries, Tile tile) {
        X = x;
        Y = y;
        Energy = energy;
        Tile = tile;
        ParentUnstagedEntries = unstagedEntries;
    }
}
