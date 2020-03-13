package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralNetwork;
import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;
import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileType;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Creature {
    private double Energy;
    private double Age;
    private double EnergyOnCurrentTile;
    private boolean CurrentTileWater;

    // Sensing
    private NeuralProperty<Double> NeuralInEnergy;
    private NeuralProperty<Double> NeuralInAge;
    private NeuralProperty<Double> NeuralInEnergyOnCurrentTile;
    private NeuralProperty<Double> NeuralInCurrentTileWater;
    // Acting
    private NeuralProperty<Double> NeuralOutRotation;
    private NeuralProperty<Double> NeuralOutForward;
    private NeuralProperty<Double> NeuralOutEat;
    private NeuralProperty<Double> NeuralOutReplicate;

    private List<Feeler> Feelers;

    private Vector3d Genetics;

    private double SpecificAgingFactor;

    private long Generation;
    private double PositionX;
    private double PositionY;
    private double Rotation;

    private Population Population;

    private NeuralNetwork brain;

    private boolean destroyed;
    private boolean inhibit;

    private boolean _draw;

    public boolean getDraw(){
        return _draw;
    }
    public void setDraw(boolean value){
        _draw = value;
    }

    private Creature() { }

    public Creature(double positionX, double positionY, Population population){
        this(positionX, positionY, 0, population);
    }

    public Creature(double positionX, double positionY, long generation, Population population) {
        Population = population;
        Generation = generation;

        SetDefaults();
        InitNewBrain();

        AddFeeler();

        PositionX = positionX;
        PositionY = positionY;

        RandomizeGenetics();
    }

    public Creature(Creature parent){
        inhibit = true;
        Generation = parent.Generation + 1;
        Population = parent.Population;
        SetDefaults();
        InheritFromParent(parent);

        Genetics = new Vector3d(parent.Genetics.x, parent.Genetics.y, parent.Genetics.z);

        MutateGenetics();
        MutateFeelers();
        MutateBrain();
    }

    private void InheritFromParent(Creature parent){
        Energy = Settings.CreatureSettings.EnergyDrainPerReplication;
        PositionX = parent.PositionX;
        PositionY = parent.PositionY;

        Feelers = new ArrayList<>();
        for (int i = 0; i < parent.Feelers.size(); i++) {
            Feelers.add(new Feeler((byte)i));

            Feelers.get(i).Angle.setValue(new Random().nextDouble() * 2 * Math.PI);
            Feelers.get(i).Length.setValue(0d);
        }

        brain = new NeuralNetwork(parent.brain);
        NeuralProperty[] ins = parent.getBrain().getInputs();
        NeuralProperty[] outs = parent.getBrain().getOutputs();
        LinkBrainFromParent(ins, outs);
    }

    private void LinkBrainFromParent(NeuralProperty[] ins, NeuralProperty[] outs){
        for (NeuralProperty in : ins) {
            switch (in.getType()) { case Bias: break;
                case EnergySelf: brain.AddInput(NeuralInEnergy, false);break;
                case Age: brain.AddInput(NeuralInAge, false);break;
                case EnergyOnCurrentTile: brain.AddInput(NeuralInEnergyOnCurrentTile, false);break;
                case CurrentTileWater: brain.AddInput(NeuralInCurrentTileWater, false);break;

                // Feelers
                case FeelsWater: brain.AddInput(Feelers.get(in.getTag()).NeuralInFeelsWater, false);break;
                case EnergyValueFeeler: brain.AddInput(Feelers.get(in.getTag()).NeuralInEnergyValueFeeler, false);break;
            }
        }

        for (NeuralProperty out : outs) {
            switch (out.getType()) {
                case Rotate: brain.AddOutput(NeuralOutRotation, false); break;
                case Forward: brain.AddOutput(NeuralOutForward, false); break;
                case Eat: brain.AddOutput(NeuralOutEat, false); break;
                case Replicate: brain.AddOutput(NeuralOutReplicate, false); break;

                //Feelers
                case FeelerAngle: brain.AddOutput(Feelers.get(out.getTag()).Angle, false); break;
                case FeelerLength: brain.AddOutput(Feelers.get(out.getTag()).Length, false); break;
            }
        }
    }

    private void InitNewBrain(){
        brain = new NeuralNetwork();

        for (int i = 0; i < Settings.CreatureSettings.BaseHiddenNeuronLayers; i++) {
            brain.AddHiddenLayer(new Random().nextInt(Settings.CreatureSettings.MutationRates.MaxHiddenNeuronsPerLayer - Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer) + Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer, false);
        }

        brain.AddInput(NeuralInEnergy, false);
        brain.AddInput(NeuralInAge, false);
        if (Settings.CreatureSettings.CanFeelOnBody)
        {
            brain.AddInput(NeuralInEnergyOnCurrentTile, false);
            brain.AddInput(NeuralInCurrentTileWater, false);
        }

        brain.AddOutput(NeuralOutRotation, false);
        brain.AddOutput(NeuralOutForward, false);
        brain.AddOutput(NeuralOutEat, false);

        // one add to trigger net rebuild;
        brain.AddOutput(NeuralOutReplicate, true);
    }

    private void SetDefaults(){
        _draw = true;

        Energy = Settings.CreatureSettings.BaseEnergy;
        Age = 0d;
        EnergyOnCurrentTile = 0d;
        CurrentTileWater = false;
        Rotation = new Random().nextDouble() * 2 * Math.PI;

        NeuralInEnergy = new NeuralProperty<Double>(Energy, NeuralPropertyType.EnergySelf);
        NeuralInAge = new NeuralProperty<Double>(Age, NeuralPropertyType.Age);
        if (Settings.CreatureSettings.CanFeelOnBody) {
            NeuralInEnergyOnCurrentTile = new NeuralProperty<>(NeuralPropertyType.EnergyOnCurrentTile);
            NeuralInCurrentTileWater = new NeuralProperty<>(NeuralPropertyType.CurrentTileWater);
        }

        NeuralOutRotation = new NeuralProperty<>(NeuralPropertyType.Rotate);
        NeuralOutForward = new NeuralProperty<>(NeuralPropertyType.Forward);
        NeuralOutEat = new NeuralProperty<>(NeuralPropertyType.Eat);
        NeuralOutReplicate = new NeuralProperty<>(NeuralPropertyType.Replicate);

        Feelers = new ArrayList<>();

        SpecificAgingFactor = 1 + (new Random().nextDouble() * Settings.CreatureSettings.AgingVariance);
    }

    private void RandomizeGenetics(){
        Random r = new Random();
        Genetics = new Vector3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
    }

    private void MutateGenetics(){
        Random r = new Random();
        Genetics.x += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (Genetics.x > 1) Genetics.x = 1; else if (Genetics.x < 0) Genetics.x = 0;

        Genetics.y += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (Genetics.y > 1) Genetics.y = 1; else if (Genetics.y < 0) Genetics.y = 0;

        Genetics.z += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (Genetics.z > 1) Genetics.z = 1; else if (Genetics.z < 0) Genetics.z = 0;
    }

    private void MutateFeelers(){
        Random r = new Random();

        if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.FeelerMutationChance) {
            if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.FeelerAddRemoveThreshold)
            {
                AddFeeler();
                //System.out.println("[MUTATION] Feeler+");
            }else {
                if (Feelers.size() > 1){
                    RemoveFeeler();
                    //System.out.println("[MUTATION] Feeler-");
                }
            }
        }
    }

    private void MutateBrain(){
        Random r = new Random();

        if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.BrainMutationChance){
            if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.HiddenLayerAddRemoveThreshold){
                brain.AddHiddenLayer(Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer + r.nextInt(Settings.CreatureSettings.MutationRates.MaxHiddenNeuronsPerLayer - Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer));
                //System.out.println("[MUTATION] Hidden Layer+");
            }
            else
            {
                //brain.RemoveLatestHiddenLayer();
                brain.RemoveRandomHiddenLayer();
                //System.out.println("[MUTATION] Hidden Layer-");
            }
        }

        brain.Mutate(Settings.CreatureSettings.MutationRates.Weights);
    }

    private void AddFeeler(){
        Feeler f = new Feeler((byte)Feelers.size());
        Feelers.add(f);

        f.Angle.setValue(new Random().nextDouble() * 2 * Math.PI);
        f.Length.setValue(Settings.CreatureSettings.InitialFeelerLength);

        LinkFeeler(f);
    }

    private void LinkFeeler(Feeler f){
        brain.AddInput(f.NeuralInFeelsWater, false);
        brain.AddInput(f.NeuralInEnergyValueFeeler, false);
        brain.AddOutput(f.Angle, false);
        brain.AddOutput(f.Length, true);
    }

    private void RemoveFeeler(){
        Feeler f = Feelers.get(Feelers.size() - 1);
        Feelers.remove(f);

        brain.RemoveInput(f.NeuralInFeelsWater, false);
        brain.RemoveInput(f.NeuralInEnergyValueFeeler, false);
        brain.RemoveOutput(f.Angle, false);
        brain.RemoveOutput(f.Length, true);
    }

    public void Sense(){
        // Energy
        NeuralInEnergy.setValue((2 * Energy / Settings.CreatureSettings.BaseEnergy) - 1);

        // Age
        NeuralInAge.setValue(Age / 60 - 1);

        Tile t = Population.getTile((int)PositionX, (int)PositionY);

        if (Settings.CreatureSettings.CanFeelOnBody){
            // EnergyOnCurrentTile
            EnergyOnCurrentTile = t.Energy;
            if (NeuralInEnergyOnCurrentTile != null)
                NeuralInEnergyOnCurrentTile.setValue((2 * EnergyOnCurrentTile / Settings.SimulationSettings.MaxEnergyPerTile) - 1);

            // CurrentTileWater
            CurrentTileWater = t.getType() == TileType.Water;
            if (NeuralInCurrentTileWater != null)
                NeuralInCurrentTileWater.setValue((CurrentTileWater ? 1d : -1d));
        }

        // Feeler
        for (Feeler feeler : Feelers) {
            int feelsOnX = (int) (Math.round(PositionX + (Math.cos(feeler.Angle.getValue()) * feeler.Length.getValue()) / Settings.WorldSettings.TileSize));
            int feelsOnY = (int) (Math.round(PositionY + (Math.sin(feeler.Angle.getValue()) * feeler.Length.getValue()) / Settings.WorldSettings.TileSize));

            t = Population.getTile(feelsOnX, feelsOnY);

            // Feeler.FeelsWater
            feeler.NeuralInFeelsWater.setValue((t.getType() == TileType.Water ? 1d : -1d));

            // Feeler.EnergyValueFeeler
            feeler.NeuralInEnergyValueFeeler.setValue((2 * EnergyOnCurrentTile / Settings.SimulationSettings.MaxEnergyPerTile) - 1);
        }
    }

    public void Act(double deltaTime) {
        if (inhibit) {
            inhibit = false;
            return;
        }

        // Age
        Age += ((deltaTime / 1000));

        // Constant Energy Cost
        Energy -= (Settings.CreatureSettings.AgeEnergyDrainPerSecond * SpecificAgingFactor * (Math.pow(Age, 2)) * deltaTime / 1000);
        if (CurrentTileWater)
            Energy -= (Settings.CreatureSettings.EnergyDrainOnWaterPerSecond * deltaTime / 1000);

        // Fat index
        if (Energy > Settings.CreatureSettings.BaseEnergy){
            Energy -= ((1/Settings.CreatureSettings.AllowedFatness) * (Energy - Settings.CreatureSettings.BaseEnergy)) / 1000;
        }

        // Eat
        Energy += Population.Eat((int)PositionX, (int)PositionY, NeuralOutEat.getValue() * Settings.CreatureSettings.MaxEatPortionPerSecond * deltaTime / 1000);

        // Rotation
        Rotation += NeuralOutRotation.getValue() * Settings.CreatureSettings.RotationRangePerSecond / 1000;

        // Forward
        double move = Settings.CreatureSettings.MovingRangePerSecond * deltaTime / 1000;
        Energy -=  (Settings.CreatureSettings.MovingEnergyDrainPerPixel * Math.abs(NeuralOutForward.getValue()) * move);

        double moveX = Math.cos(Rotation) * (NeuralOutForward.getValue() * move);
        double moveY = Math.sin(Rotation) * (NeuralOutForward.getValue() * move);

        PositionX += moveX;
        PositionY += moveY;

        // Replication
        if (Age >= Settings.CreatureSettings.ReplicationMinAge){
            if (NeuralOutReplicate.getValue() > 0){
                Energy -= Settings.CreatureSettings.EnergyDrainPerReplication;

                if (Energy > 0)
                    Replicate();
            }
        }

        // Feeler
        Energy -= (Settings.CreatureSettings.EnergyDrainPerFeelerPerSecond * Feelers.size() * deltaTime / 1000);

        for (Feeler f : Feelers){
            // Feeler.Angle
            f.Angle.setValue(f.Angle.getValue() * 2 * Math.PI + NeuralOutRotation.getValue());

            // Feeler.Length
            f.Length.setValue(f.Length.getValue() * Settings.CreatureSettings.MaxFeelerLength);
            if (f.Length.getValue() < Settings.CreatureSettings.MinFeelerLength)
                f.Length.setValue(Settings.CreatureSettings.MinFeelerLength);

            Energy -= (Settings.CreatureSettings.EnergyDrainPerFeelerLengthPerSecond *
                        Math.pow(f.Length.getValue() - Settings.CreatureSettings.MinFeelerLength,
                                Settings.CreatureSettings.EnergyDrainPerFeelerLengthExponent) * deltaTime / 1000);
        }

        if (Energy <= 0)
            Destroy();
    }

    private void Destroy() {
        brain.Destroy();
        Feelers.clear();

        Population.RemoveCreature(this);

        destroyed = true;
    }

    private void Replicate(){
        Population.AddCreature(new Creature(this));
    }

    public NeuralNetwork getBrain() { return brain; }

    public double getRotation(){
        return Rotation;
    }
    public void setRotation(double value){
        Rotation = value;
    }

    public double getPositionY(){
        return PositionY;
    }
    public void setPositionY(double value){
        PositionY = value;
    }

    public double getPositionX(){
        return PositionX;
    }
    public void setPositionX(double value){
        PositionX = value;
    }

    public int getPositionYi(){ return (int)PositionY; }
    public void setPositionYi(int value){
        PositionY = value;
    }

    public int getPositionXi(){
        return (int)PositionX;
    }
    public void setPositionXi(int value){
        PositionX = value;
    }

    public double getEnergy(){
        return Energy;
    }
    public void setEnergy(double value){
        Energy = value;
    }

    public double getAge() {
        return Age;
    }
    public void setAge(double age) {
        Age = age;
    }

    public long getGeneration() {
        return Generation;
    }

    public Vector3d getGenetics() {
        return Genetics;
    }

    public List<Feeler> getFeelers() {
        return Feelers;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean inhibits(){
        return inhibit;
    }
}
