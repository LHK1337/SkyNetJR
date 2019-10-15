package SkyNetJR.AI;

public enum NeuralPropertyType {
    //* Inputs
    // General
    Bias,
    EnergySelf,
    Age,
    EnergyOnCurrentTile,
    CurrentTileWater,
    WasAttacked,
    WasHealed,

    //Feeler
    FeelsWater,
    EnergyValueFeeler,
    FeelsCreature,
    GeneticDifference,
    OtherCreatureAge,
    OtherCreatureEnergy,


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
