/*
* Beschreibt Logik der Fühler der Kreaturen
* */

package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;

public class Feeler {
    public byte _tag;    // Identifikation des Fühlers

    public Feeler(byte tag){
        _tag = tag;

        NeuralInFeelsWater = new NeuralProperty<>(NeuralPropertyType.FeelsWater, _tag);
        NeuralInEnergyValueFeeler = new NeuralProperty<>(NeuralPropertyType.EnergyValueFeeler, _tag);
        Angle = new NeuralProperty<>(NeuralPropertyType.FeelerAngle, _tag);
        Length = new NeuralProperty<>(NeuralPropertyType.FeelerLength, _tag);
    }

    // Sensing
    public NeuralProperty<Double> NeuralInFeelsWater;
    public NeuralProperty<Double> NeuralInEnergyValueFeeler;

    // Acting
    public NeuralProperty<Double> Angle;
    public NeuralProperty<Double> Length;
}
