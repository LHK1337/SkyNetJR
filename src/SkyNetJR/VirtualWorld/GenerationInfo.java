package SkyNetJR.VirtualWorld;

import SkyNetJR.Settings;

public class GenerationInfo {
    public int Seed;
    public Double LandThreshold;
    public int Octaves;
    public int StartFrequencyX;
    public int StartFrequencyY;

    public GenerationInfo() {
    }

    public static GenerationInfo GetDefaults() {
        GenerationInfo g = new GenerationInfo();
        g.LandThreshold = Settings.GenerationSettings.LandThreshold;
        g.Seed = Settings.GenerationSettings.Seed;
        g.Octaves = Settings.GenerationSettings.Octaves;
        g.StartFrequencyX = Settings.GenerationSettings.StartFrequencyX;
        g.StartFrequencyY = Settings.GenerationSettings.StartFrequencyY;

        return g;
    }
}
