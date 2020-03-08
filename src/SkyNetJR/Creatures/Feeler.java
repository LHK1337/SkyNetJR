package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;

public class Feeler {
    public byte Tag;

    public Feeler(byte tag){
        Tag = tag;

        NeuralInFeelsWater = new NeuralProperty<>(NeuralPropertyType.FeelsWater, Tag);
        NeuralInEnergyValueFeeler = new NeuralProperty<>(NeuralPropertyType.EnergyValueFeeler, Tag);
        Angle = new NeuralProperty<>(NeuralPropertyType.FeelerAngle, Tag);
        Length = new NeuralProperty<>(NeuralPropertyType.FeelerLength, Tag);
    }

    // Sensing
    public NeuralProperty<Double> NeuralInFeelsWater;
    public NeuralProperty<Double> NeuralInEnergyValueFeeler;

    // Acting
    public NeuralProperty<Double> Angle;
    public NeuralProperty<Double> Length;
}
