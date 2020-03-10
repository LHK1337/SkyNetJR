package SkyNetJR;

import org.joml.Vector3d;

public class Settings {
    // World Generation Settings
    public static class GenerationSettings {
        public static final double LandThreshold = 0.7d;
        public static final int Octaves = 8;
        public static final int StartFrequencyX = 4;
        public static final int StartFrequencyY = 4;
    }

    // World Settings
    public static class WorldSettings {
        public static final int Width = 300;
        public static final int Height = 184;
        public static final int TileSize = 5;
        public static final Vector3d MaxEnergyColor = new Vector3d(0.25d, 0.8d, 0d);
        public static final Vector3d WaterColor = new Vector3d(0d, 0.5d, 1d);
    }

    // World Simulation Settings
    public static class SimulationSettings {
        public static final int TimePrecision = 30;
        public static final int BaseInfluence = 0;
        public static final double WaterInfluence = 3;
        public static final double OutGrownTileInfluence = 0.2;
        public static final double TileInfluenceThreshold = 0.99d;
        public static final int MaxEnergyPerTile = 30;
        public static final double BaseEnergyGeneration = 1d;
        public static final double StartEnergy = 20;
        public static final double RandomEnergyGenerationChance = 0.001d;
        public static final double RandomEnergyGeneration = 3d;
    }

    // View Settings
    public static class ViewSettings {
        public static final int Width = 1500;
        public static final int Height = 920;
    }

    // Creature Settings
    public static class CreatureSettings {
        public static final double AllowedFatness = 1.5d;
        public static final boolean CanFeelOnBody = true;
        public static final double BaseEnergy = 300d;
        public static final double AgingVariance = 4;
        public static final double ReplicationMinAge = 10d;
        public static final double EnergyDrainPerSecond = 4d;
        public static final double EnergyDrainOnWaterPerSecond = 800d;
        public static final double EnergyDrainPerReplication = 600d;
        public static final double AgeEnergyDrainPerSecond = 0.01d;
        public static final double MovingEnergyDrainPerPixel = 0.5d;
        public static final double EnergyDrainPerFeelerPerSecond = 10d;
        public static final double EnergyDrainPerFeelerLengthPerSecond = 0.1d;
        public static final double EnergyDrainPerFeelerLengthExponent = 1d;
        public static final double MovingRangePerSecond = 20;
        public static final double RotationRangePerSecond = 2 * Math.PI;
        public static final double MaxEatPortionPerSecond = 2000;
        public static final int CreatureSize = 8;
        public static final int MinPopulationSize = 5;
        public static final int InitialPopulationSizeTarget = 300;
        public static final double InitialFeelerLength = 8;
        public static final double MinFeelerLength = 8;
        public static final double MaxFeelerLength = 12;
        public static final int BaseHiddenNeuronLayers = 0;


        public static class MutationRates{
            public static final double InitialWeightRange = 1d;
            public static final double Genetics = 0.05d;
            public static final double Weights = 0.01d;
            public static final double FeelerMutationChance = 0.09d;
            public static final double FeelerAddRemoveThreshold = 0.5d;
            public static final double BrainMutationChance = 0.08d;
            public static final double HiddenLayerAddRemoveThreshold = 0.5d;
            public static final int MaxHiddenNeuronsPerLayer = 10;
            public static final int MinHiddenNeuronsPerLayer = 4;
        }
    }
}