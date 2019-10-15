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
        public static final Vector3d MaxEnergyColor = new Vector3d(0.25d, 0.8d, 0d);
        public static final Vector3d WaterColor = new Vector3d(0d, 0.5d, 1d);
    }

    // World Simulation Settings
    public static class SimulationSettings {
        public static final int TimePrecision = 50;
        public static final int BaseInfluence = 0;
        public static final double WaterInfluence = 20;
        public static final double OutGrownTileInfluence = 3;
        public static final double TileInfluenceThreshold = 0.5d;
        public static final int MaxEnergyPerTile = 100;
        public static final double BaseEnergyGeneration = 3d;
        public static final double StartEnergy = 20;
        public static final double RandomEnergyGenerationChance = 0.002d;
        public static final double RandomEnergyGeneration = 10d;
    }

    // View Settings
    public static class ViewSettings {
        public static final int Width = 1280;
        public static final int Height = 720;
    }

    // Creature Settings
    public static class CreatureSettings {
        public static final double BaseEnergy = 800d;
        public static final double ReplicationMinAge = 10d;
        public static final double EnergyDrainPerSecond = 2d;
        public static final double EnergyDrainOnWaterPerSecond = 200d;
        public static final double EnergyDrainPerReplication = 1000d;
        public static final double AgeEnergyDrainPerSecond = 0.01d;
        public static final double MovingEnergyDrainPerPixel = 1d;
        public static final double EnergyDrainPerFeelerPerSecond = 0.05d;
        public static final double EnergyDrainPerFeelerLengthPerSecond = 0.1d;
        public static final double EnergyDrainPerFeelerLengthExponent = 1d;
        public static final double RotationRange = 0.5 * Math.PI;
        public static final double MovingRange = 5;
        public static final double MaxEatPortion = 30;
        public static final int CreatureSize = 8;
        public static final int MinPopulationSize = 50;
        public static final int InitialPopulationSize = 150;
        public static final double InitialFeelerLength = 5;

        public static class MutationRates{
            public static final double InitialWeightRange = 0.001d;
            public static final double Genetics = 0.05d;
            public static final double Weights = 0.1d;
            public static final double FeelerMutationChance = 0.03d;
            public static final double FeelerAddRemoveThreshold = 0.5d;
            public static final double BrainMutationChance = 0.02d;
            public static final double HiddenLayerAddRemoveThreshold = 0.5d;
            public static final int MaxHiddenNeuronsPerLayer = 5;
        }
    }
}
