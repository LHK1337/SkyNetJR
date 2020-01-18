package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;

public class Feeler {
    public byte Tag;

    public Feeler(byte tag){
        Tag = tag;

        NeuralInFeelsWater = new NeuralProperty<>(NeuralPropertyType.FeelsWater, Tag);
        NeuralInEnergyValueFeeler = new NeuralProperty<>(NeuralPropertyType.EnergyValueFeeler, Tag);
        NeuralInFeelsCreature = new NeuralProperty<>(NeuralPropertyType.FeelsCreature, Tag);
        NeuralInGeneticDifference = new NeuralProperty<>(NeuralPropertyType.GeneticDifference, Tag);
        NeuralInOtherCreatureAge = new NeuralProperty<>(NeuralPropertyType.OtherCreatureAge, Tag);
        NeuralInOtherCreatureEnergy = new NeuralProperty<>(NeuralPropertyType.OtherCreatureEnergy, Tag);
        Angle = new NeuralProperty<>(NeuralPropertyType.FeelerAngle, Tag);
        Length = new NeuralProperty<>(NeuralPropertyType.FeelerLength, Tag);
        Attack = new NeuralProperty<>(NeuralPropertyType.Attack, Tag);
        Heal = new NeuralProperty<>(NeuralPropertyType.Heal, Tag);
    }

    // Sensing
    public NeuralProperty<Double> NeuralInFeelsWater;
    public NeuralProperty<Double> NeuralInEnergyValueFeeler;
    public NeuralProperty<Double> NeuralInFeelsCreature;
    public NeuralProperty<Double> NeuralInGeneticDifference;
    public NeuralProperty<Double> NeuralInOtherCreatureAge;
    public NeuralProperty<Double> NeuralInOtherCreatureEnergy;

    // Acting
    public NeuralProperty<Double> Angle;
    public NeuralProperty<Double> Length;
    public NeuralProperty<Double> Attack;
    public NeuralProperty<Double> Heal;
}
