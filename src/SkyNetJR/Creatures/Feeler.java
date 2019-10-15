package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;

public class Feeler {
    public byte Tag;

    public Feeler(byte tag){
        Tag = tag;

        FeelsWater = new NeuralProperty<>(NeuralPropertyType.FeelsWater, Tag);
        EnergyValueFeeler = new NeuralProperty<>(NeuralPropertyType.EnergyValueFeeler, Tag);
        FeelsCreature = new NeuralProperty<>(NeuralPropertyType.FeelsCreature, Tag);
        GeneticDifference = new NeuralProperty<>(NeuralPropertyType.GeneticDifference, Tag);
        OtherCreatureAge = new NeuralProperty<>(NeuralPropertyType.OtherCreatureAge, Tag);
        OtherCreatureEnergy = new NeuralProperty<>(NeuralPropertyType.OtherCreatureEnergy, Tag);
        Angle = new NeuralProperty<>(NeuralPropertyType.FeelerAngle, Tag);
        Length = new NeuralProperty<>(NeuralPropertyType.FeelerLength, Tag);
        Attack = new NeuralProperty<>(NeuralPropertyType.Attack, Tag);
        Heal = new NeuralProperty<>(NeuralPropertyType.Heal, Tag);
    }

    // Sensing
    public NeuralProperty<Double> FeelsWater;
    public NeuralProperty<Double> EnergyValueFeeler;
    public NeuralProperty<Double> FeelsCreature;
    public NeuralProperty<Double> GeneticDifference;
    public NeuralProperty<Double> OtherCreatureAge;
    public NeuralProperty<Double> OtherCreatureEnergy;

    // Acting
    public NeuralProperty<Double> Angle;
    public NeuralProperty<Double> Length;
    public NeuralProperty<Double> Attack;
    public NeuralProperty<Double> Heal;
}
