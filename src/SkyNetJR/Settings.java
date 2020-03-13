package SkyNetJR;

import org.joml.Vector3d;

public class Settings {
    // Eigenschaften der Terraingeneration
    public static class GenerationSettings {
        // Schwellenwert für Land oder Wasser in der Terraingeneration
        public static final double LandThreshold = 0.7d;
        // Konstanten für die Terraingeneration
        public static final int Octaves = 8;
        public static final int StartFrequencyX = 4;
        public static final int StartFrequencyY = 4;
    }

    // Eigenschaften der virtuellen Welt
    public static class WorldSettings {
        public static final int Width = 300;
        public static final int Height = 184;
        // Größe der Tiles in Pixel
        public static final int TileSize = 5;
        // Farben für das Terrain
        public static final Vector3d MaxEnergyColor = new Vector3d(0.25d, 0.8d, 0d);
        public static final Vector3d WaterColor = new Vector3d(0d, 0.5d, 1d);
    }

    // World Simulation Settings
    public static class SimulationSettings {
        // Genauigkeit der Simulation (Zeiteinheit für die Berechnung der Simulation, kleiner = genauer)
        public static final int TimePrecision = 30;
        //// Einfluss steuert, wie schnell Energy in der Welt nachwächst
        // Grundeinfluss
        public static final int BaseInfluence = 0;
        // Einfluss durch Wasser
        public static final double WaterInfluence = 3;
        // Einfluss durch volle benachbarte Tiles
        public static final double OutGrownTileInfluence = 0.2;
        // Schwellenwert, wann ein Tile zu Genüge für benachbarten Einfluss gefüllt ist
        public static final double TileInfluenceThreshold = 0.99d;
        // Maximale Energie pro Tile
        public static final int MaxEnergyPerTile = 30;
        // Grundenergiegeneration
        public static final double BaseEnergyGeneration = 1d;
        // Anfangsenergie der Tiles
        public static final double StartEnergy = 20;
        // Zufällige Energiegenerationschance
        public static final double RandomEnergyGenerationChance = 0.001d;
        // Menge der zufälligen Energiegenerations
        public static final double RandomEnergyGeneration = 3d;
    }

    // Eigenschaften der Weltansicht
    public static class ViewSettings {
        public static final int Width = 1500;
        public static final int Height = 920;
    }

    // Eigenschaften der Kreaturen
    public static class CreatureSettings {
        // Erlaubtes Übergewicht
        public static final double AllowedFatness = 1.5d;
        // Spürt Wasser und Energie neben Fühler auch auf Körper
        public static final boolean CanFeelOnBody = true;
        // Grundenergie
        public static final double BaseEnergy = 300d;
        // mögliche Alterungsstreuung
        public static final double AgingVariance = 4;
        // Mindestalter für Replikation
        public static final double ReplicationMinAge = 10d;
        // Grundumsatz pro Sekunde
        public static final double EnergyDrainPerSecond = 4d;
        // Grundumsatz auf Wasser pro Sekunde
        public static final double EnergyDrainOnWaterPerSecond = 800d;
        // Energie für Replikation
        public static final double EnergyDrainPerReplication = 600d;
        // Grundumsatzzusatz basierend auf dem Alter pro Sekunde
        public static final double AgeEnergyDrainPerSecond = 0.01d;
        // Bewegungsenergiekosten
        public static final double MovingEnergyDrainPerPixel = 0.5d;
        // Energiekosten für Fühler pro Sekunde
        public static final double EnergyDrainPerFeelerPerSecond = 10d;
        // Energiekosten für Fühlerlänge pro Sekunde
        public static final double EnergyDrainPerFeelerLengthPerSecond = 0.1d;
        // Energiekostenexponent für Fühlerlänge
        public static final double EnergyDrainPerFeelerLengthExponent = 1d;
        // Maximale Reichweite in einer Sekunde
        public static final double MovingRangePerSecond = 20;
        // Maximale Rotation in einer Sekunde
        public static final double RotationRangePerSecond = 2 * Math.PI;
        // Maximale Enerhieaufnahme in einer Sekunde
        public static final double MaxEatPortionPerSecond = 2000;
        // Größe der Kreatur
        public static final int CreatureSize = 8;
        // Mindestgröße der Population
        public static final int MinPopulationSize = 5;
        // Zielumfang der Population
        public static final int InitialPopulationSizeTarget = 300;
        // Anfangsfühlerlänge
        public static final double InitialFeelerLength = 8;
        // Minimale Fühlerlänge
        public static final double MinFeelerLength = 8;
        // Maximale Fühlerlänge
        public static final double MaxFeelerLength = 12;
        // Anfängliche Anzahl an versteckten Schichten im Gehirn einer Kreatur
        public static final int BaseHiddenNeuronLayers = 0;

        // Konstanten der Mutation
        public static class MutationRates{
            // Anfangsbereich der Wichtungen
            public static final double InitialWeightRange = 1d;
            // Mutationskoeffizient Genidentifikation
            public static final double Genetics = 0.05d;
            // Mutationskoeffizient Wichtung
            public static final double Weights = 0.01d;
            // Chance die Anzahl an Fühlern zu mutieren
            public static final double FeelerMutationChance = 0.09d;
            // Chance einen Fühler zu erhalten oder zu verlieren (im Falle einer Mutation)
            public static final double FeelerAddRemoveThreshold = 0.5d;
            // Chance die Anzahl an versteckten Schichten zu mutieren
            public static final double BrainMutationChance = 0.08d;
            // Chance eine versteckte Schicht zu erhalten oder zu verlieren (im Falle einer Mutation)
            public static final double HiddenLayerAddRemoveThreshold = 0.5d;
            // Mindestanzahl an Neuronen pro Schicht
            public static final int MaxHiddenNeuronsPerLayer = 10;
            // Maximalanzahl an Neuronen pro Schicht
            public static final int MinHiddenNeuronsPerLayer = 4;
        }
    }
}