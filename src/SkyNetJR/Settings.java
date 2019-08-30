package SkyNetJR;

import java.util.Random;

public class Settings {
    // World Generation Settings
    public static class GenerationSettings {
        public static final double LandThreshold = 0.6d;
        public static final int Seed = new Random().nextInt();
        public static final int Octaves = 8;
        public static final int StartFrequencyX = 4;
        public static final int StartFrequencyY = 4;
    }

    // World Settings
    public static class WorldSettings {
        public static final int Width = 50;
        public static final int Height = 36;
        public static final int TileSize = 20;
        public static final int MaxEnergyPerTile = 100;
        public static final double BaseEnergyGeneration = 0.6d;
        public static final double StartEnergy = 0;
    }

    // World Simulation Settings
    public static class SimulationSettings {
        public static final int TimePrecision = 50;
        public static final int BaseInfluence = 0;
        public static final double WaterInfluence = 30;
        public static final double MaxGrownTileInfluence = 25;
        public static final double TileInfluenceThreshold = 0.2d;
    }

    // View Settings
    public static class ViewSettings {
        public static final int Width = 1280;
        public static final int Height = 720;
    }

}
