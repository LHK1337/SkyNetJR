package SkyNetJR.AI;

public enum NeuralPropertyType {
    //* Inputs
    // General
    Bias,
    EnergySelf,
    Age,
    EnergyOnCurrentTile,
    CurrentTileWater,

    //Feeler
    FeelsWater,
    EnergyValueFeeler,


    //* Outputs
    // General
    Rotate,
    Forward,
    Eat,
    Replicate,

    //Feeler
    FeelerAngle,
    FeelerLength,
    Attack,
    Heal
}
