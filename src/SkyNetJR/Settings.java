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
        public static final int Width = 640;
        public static final int Height = 360;
        public static final int TileSize = 2;
        public static final Vector3d MaxEnergyColor = new Vector3d(0.25d, 0.8d, 0d);
        public static final Vector3d WaterColor = new Vector3d(0d, 0.5d, 1d);
    }

    // World Simulation Settings
    public static class SimulationSettings {
        public static final int TimePrecision = 60;
        public static final int BaseInfluence = 0;
        public static final double WaterInfluence = 12;
        public static final double OutGrownTileInfluence = 0.5;
        public static final double TileInfluenceThreshold = 0.2d;
        public static final int MaxEnergyPerTile = 60;
        public static final double BaseEnergyGeneration = 10d;
        public static final double StartEnergy = 0;
        public static final double RandomEnergyGenerationChance = 0.001d;
        public static final double RandomEnergyGeneration = 5d;
    }

    // View Settings
    public static class ViewSettings {
        public static final int Width = 1280;
        public static final int Height = 720;
    }

    // Creature Settings
    public static class CreatureSettings {
        public static final boolean CanFeelOnBody = false;
        public static final double BaseEnergy = 400d;
        public static final double AgingVariance = 10;
        public static final double ReplicationMinAge = 10d;
        public static final double EnergyDrainPerSecond = 4d;
        public static final double EnergyDrainOnWaterPerSecond = 800d;
        public static final double EnergyDrainPerReplication = 600d;
        public static final double AgeEnergyDrainPerSecond = 0.01d;
        public static final double MovingEnergyDrainPerPixel = 0.5d;
        public static final double EnergyDrainPerFeelerPerSecond = 0.05d;
        public static final double EnergyDrainPerFeelerLengthPerSecond = 0.01d;
        public static final double EnergyDrainPerFeelerLengthExponent = 1d;
        public static final double RotationRangePerSecond = 2 * Math.PI;
        public static final double MovingRangePerSecond = 120;
        public static final double MaxEatPortionPerSecond = 1200;
        public static final int CreatureSize = 5;
        public static final int MinPopulationSize = 25;
        public static final int InitialPopulationSize = 300;
        public static final double InitialFeelerLength = 8;
        public static final double MinFeelerLength = 3;
        public static final double MaxFeelerLength = 8;
        public static final int BaseHiddenNeurons = 5;


        public static class MutationRates{
            public static final double InitialWeightRange = 0.01d;
            public static final double Genetics = 0.004d;
            public static final double Weights = 0.05d;
            public static final double FeelerMutationChance = 0.009d;
            public static final double FeelerAddRemoveThreshold = 0.5d;
            public static final double BrainMutationChance = 0.0008d;
            public static final double HiddenLayerAddRemoveThreshold = 0.5d;
            public static final int MaxHiddenNeuronsPerLayer = 8;
        }
    }
}
