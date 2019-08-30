package SkyNetJR;

import org.joml.Vector3d;

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
        public static final int Width = 256;
        public static final int Height = 180;
        public static final int TileSize = 4;
        public static final int MaxEnergyPerTile = 100;
        public static final double BaseEnergyGeneration = 0.6d;
        public static final double StartEnergy = 0;
        public static final double RandomEnergyGenerationChance = 0.02d;
        public static final double RandomEnergyGeneration = 1.0d;
        public static final Vector3d MaxEnergyColor = new Vector3d(0.25d, 0.8d, 0d);
        public static final Vector3d WaterColor = new Vector3d(0d, 0.5d, 1d);
    }

    // World Simulation Settings
    public static class SimulationSettings {
        public static final int TimePrecision = 50;
        public static final int BaseInfluence = 0;
        public static final double WaterInfluence = 20;
        public static final double OutGrownTileInfluence = 10;
        public static final double TileInfluenceThreshold = 0.1d;
    }

    // View Settings
    public static class ViewSettings {
        public static final int Width = 1280;
        public static final int Height = 720;
    }

}
