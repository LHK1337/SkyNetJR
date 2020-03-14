/*
* Auflistung aller verfügbaren Typen von NeuralProperties
* */

package SkyNetJR.AI;

public enum NeuralPropertyType {
    //* Inputs
    // General
    Bias                    (0),
    EnergySelf              (1),
    Age                     (2),
    EnergyOnCurrentTile     (3),
    CurrentTileWater        (4),

    //Feeler
    FeelsWater              (5),
    EnergyValueFeeler       (6),


    //* Outputs
    // General
    Rotate                  (7),
    Forward                 (8),
    Eat                     (9),
    Replicate               (10),

    //Feeler
    FeelerAngle             (11),
    FeelerLength            (12),
    Attack                  (13),
    Heal                    (14);

    private final byte _VALUE;
    private NeuralPropertyType(byte value) {
        this._VALUE = value;
    }
    private NeuralPropertyType(int value) {
        this._VALUE = (byte)value;
    }

    public byte getValue() {
        return _VALUE;
    }

    // Cachen der aufgelisteten Werten, um Funktionsaufruf zu beschleunigen
    private static NeuralPropertyType[] _Cache =  NeuralPropertyType.values();
    public static NeuralPropertyType FromByte(byte value){
        return _Cache[value];
    }
}
