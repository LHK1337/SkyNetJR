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
    // Sensing
    private NeuralProperty<Double> Energy;
    private NeuralProperty<Double> Age;
    private NeuralProperty<Double> EnergyOnCurrentTile;
    private NeuralProperty<Double> CurrentTileWater;
    private NeuralProperty<Double> WasAttacked;
    private NeuralProperty<Double> WasHealed;

    // Acting
    private NeuralProperty<Double> RotationChange;
    private NeuralProperty<Double> Forward;
    private NeuralProperty<Double> Eat;
    private NeuralProperty<Double> Replicate;

    private List<Feeler> Feelers;

    private Vector3d Genetics;

    private double SpecificAgingFactor;

    private final long Generation;
    private double PositionX;
    private double PositionY;
    private double Rotation;

    private Population Population;

    private NeuralNetwork brain;

    private boolean destroyed;
    private boolean inhibit;

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
        Energy.setValue(Settings.CreatureSettings.EnergyDrainPerReplication);
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
        for (NeuralProperty in : ins) {
            switch (in.getType()) { case Bias: break;
                case EnergySelf: brain.AddInput(Energy, false);break;
                case Age: brain.AddInput(Age, false);break;
                case EnergyOnCurrentTile: brain.AddInput(EnergyOnCurrentTile, false);break;
                case CurrentTileWater: brain.AddInput(CurrentTileWater, false);break;
                case WasAttacked: brain.AddInput(WasAttacked, false);break;
                case WasHealed: brain.AddInput(WasHealed, false);break;

                // Feelers
                case FeelsWater: brain.AddInput(Feelers.get(in.getTag()).FeelsWater, false);break;
                case EnergyValueFeeler: brain.AddInput(Feelers.get(in.getTag()).EnergyValueFeeler, false);break;
                case FeelsCreature: brain.AddInput(Feelers.get(in.getTag()).FeelsCreature, false);break;
                case GeneticDifference: brain.AddInput(Feelers.get(in.getTag()).GeneticDifference, false);break;
                case OtherCreatureAge: brain.AddInput(Feelers.get(in.getTag()).OtherCreatureAge, false);break;
                case OtherCreatureEnergy: brain.AddInput(Feelers.get(in.getTag()).OtherCreatureEnergy, false);break;
            }
        }

        NeuralProperty[] outs = parent.getBrain().getOutputs();
        for (NeuralProperty out : outs) {
            switch (out.getType()) {
                case Rotate: brain.AddOutput(RotationChange, false); break;
                case Forward: brain.AddOutput(Forward, false); break;
                case Eat: brain.AddOutput(Eat, false); break;
                case Replicate: brain.AddOutput(Replicate, false); break;

                //Feelers
                case FeelerAngle: brain.AddOutput(Feelers.get(out.getTag()).Angle, false); break;
                case FeelerLength: brain.AddOutput(Feelers.get(out.getTag()).Length, false); break;
                case Attack: brain.AddOutput(Feelers.get(out.getTag()).Attack, false); break;
                case Heal: brain.AddOutput(Feelers.get(out.getTag()).Heal, false); break;
            }
        }
    }

    private void InitNewBrain(){
        brain = new NeuralNetwork();

        brain.AddHiddenLayer(Settings.CreatureSettings.BaseHiddenNeurons, false);

        brain.AddInput(Energy, false);
        brain.AddInput(Age, false);
        if (Settings.CreatureSettings.CanFeelOnBody)
        {
            brain.AddInput(EnergyOnCurrentTile, false);
            brain.AddInput(CurrentTileWater, false);
        }
        brain.AddInput(WasAttacked, false);
        brain.AddInput(WasHealed, false);
        brain.AddOutput(RotationChange, false);
        brain.AddOutput(Forward, false);
        brain.AddOutput(Eat, false);

        // one add to trigger net rebuild;
        brain.AddOutput(Replicate, true);
    }

    private void SetDefaults(){
        Energy = new NeuralProperty<Double>(Settings.CreatureSettings.BaseEnergy, NeuralPropertyType.EnergySelf);
        Age = new NeuralProperty<Double>(0d, NeuralPropertyType.Age);
        EnergyOnCurrentTile = new NeuralProperty<>(NeuralPropertyType.EnergyOnCurrentTile);
        CurrentTileWater = new NeuralProperty<>(NeuralPropertyType.CurrentTileWater);
        WasAttacked = new NeuralProperty<>(NeuralPropertyType.WasAttacked);
        WasHealed = new NeuralProperty<>(NeuralPropertyType.WasHealed);

        RotationChange = new NeuralProperty<>(NeuralPropertyType.Rotate);
        Forward = new NeuralProperty<>(NeuralPropertyType.Forward);
        Eat = new NeuralProperty<>(NeuralPropertyType.Eat);
        Replicate = new NeuralProperty<>(NeuralPropertyType.Replicate);

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
                brain.AddHiddenLayer(1 + r.nextInt(Settings.CreatureSettings.MutationRates.MaxHiddenNeuronsPerLayer - 1));
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
        brain.AddInput(f.FeelsWater, false);
        brain.AddInput(f.EnergyValueFeeler, false);
        brain.AddInput(f.FeelsCreature, false);
        brain.AddInput(f.GeneticDifference, false);
        brain.AddInput(f.OtherCreatureAge, false);
        brain.AddInput(f.OtherCreatureEnergy, false);
        brain.AddOutput(f.Angle, false);
        brain.AddOutput(f.Length, false);
        brain.AddOutput(f.Attack, false);
        brain.AddOutput(f.Heal, true);
    }

    private void RemoveFeeler(){
        Feeler f = Feelers.get(Feelers.size() - 1);
        Feelers.remove(f);

        brain.RemoveInput(f.FeelsWater, false);
        brain.RemoveInput(f.EnergyValueFeeler, false);
        brain.RemoveInput(f.FeelsCreature, false);
        brain.RemoveInput(f.GeneticDifference, false);
        brain.RemoveInput(f.OtherCreatureAge, false);
        brain.RemoveInput(f.OtherCreatureEnergy, false);
        brain.RemoveOutput(f.Angle, false);
        brain.RemoveOutput(f.Length, false);
        brain.RemoveOutput(f.Attack, false);
        brain.RemoveOutput(f.Heal, true);
    }

    public void Sense(){
        // Energy - as it is
        // Age - as it is

        Tile t = Population.getTile((int)PositionX, (int)PositionY);

        // EnergyOnCurrentTile
        EnergyOnCurrentTile.setValue(t.Energy);

        // CurrentTileWater
        CurrentTileWater.setValue((t.getType() == TileType.Water ? 1d : 0d));

        // TODO WasAttacked
        WasAttacked.setValue(0d);

        // TODO WasHealed
        WasHealed.setValue(0d);

        // Feeler
        for (Feeler feeler : Feelers) {
            int feelsOnX = (int) (Math.round(PositionX + Math.cos(feeler.Angle.getValue()) / feeler.Length.getValue()));
            int feelsOnY = (int) (Math.round(PositionY + Math.sin(feeler.Angle.getValue()) / feeler.Length.getValue()));

            t = Population.getTile(feelsOnX, feelsOnY);

            // Feeler.FeelsWater
            feeler.FeelsWater.setValue((t.getType() == TileType.Water ? 1d : 0d));

            // Feeler.EnergyValueFeeler
            feeler.EnergyValueFeeler.setValue(t.Energy);

            Creature c = Population.getCollidingCreature(feelsOnX, feelsOnY);

            // Feeler.FeelsCreature
            feeler.FeelsCreature.setValue((c == null ? 0d : 1d));

            // Feeler.GeneticDifference
            if (c != null)
            {
                Vector3d otherGenetics = c.getGenetics();
                double difference = Vector3d.distance
                        (this.Genetics.x, this.Genetics.y, this.Genetics.z,
                         otherGenetics.x, otherGenetics.y, otherGenetics.z);

                feeler.GeneticDifference.setValue(difference);
            } else feeler.GeneticDifference.setValue(0d);

            // Feeler.OtherCreatureAge
            feeler.OtherCreatureAge.setValue(c != null ? c.Age.getValue() : 0d);

            // Feeler.OtherCreatureEnergy
            feeler.OtherCreatureEnergy.setValue(c != null ? c.Energy.getValue() : 0d);
        }
    }

    public void Act(double deltaTime) {
        if (inhibit) {
            inhibit = false;
            return;
        }

        // Age
        Age.setValue(Age.getValue() + (deltaTime / 1000));

        // Constant Energy Cost
        Energy.setValue(Energy.getValue() - (Settings.CreatureSettings.EnergyDrainPerSecond * deltaTime / 1000));
        Energy.setValue(Energy.getValue() - (Settings.CreatureSettings.AgeEnergyDrainPerSecond * SpecificAgingFactor * Age.getValue() * deltaTime / 1000));
        if (CurrentTileWater.getValue() > 0)
            Energy.setValue(Energy.getValue() - (Settings.CreatureSettings.EnergyDrainOnWaterPerSecond * deltaTime / 1000));

        // Eat
        // Todo: Test case when Eat < 0
        Energy.setValue(Energy.getValue() + Population.Eat((int)PositionX, (int)PositionY, Eat.getValue() * Settings.CreatureSettings.MaxEatPortionPerSecond * deltaTime / 1000));

        // Rotation
        Rotation += RotationChange.getValue() * Settings.CreatureSettings.RotationRangePerSecond * deltaTime / 1000;

        // Forward
        double move = Settings.CreatureSettings.MovingRangePerSecond * deltaTime / 1000;
        Energy.setValue(Energy.getValue() - (Settings.CreatureSettings.MovingEnergyDrainPerPixel * Math.abs(Forward.getValue()) * move));

        double moveX = Math.cos(Rotation) * (Forward.getValue() * move);
        double moveY = Math.sin(Rotation) * (Forward.getValue() * move);

        PositionX += moveX;
        PositionY += moveY;

        Population.UpdateCollisionGrid(this, (int) PositionX, (int) PositionY);

        // Replication
        if (Age.getValue() >= Settings.CreatureSettings.ReplicationMinAge){
            if (Replicate.getValue() > 0){
                Energy.setValue(Energy.getValue() - Settings.CreatureSettings.EnergyDrainPerReplication);

                if (Energy.getValue() > 0)
                    Replicate();
            }
        }

        // Feeler
        Energy.setValue(Energy.getValue() - (Settings.CreatureSettings.EnergyDrainPerFeelerPerSecond * Feelers.size() * deltaTime / 1000));

        for (Feeler f : Feelers){
            // Feeler.Angle
            f.Angle.setValue(f.Angle.getValue() * 2 * Math.PI + Rotation);

            // Feeler.Length
            f.Length.setValue(f.Length.getValue() * Settings.CreatureSettings.MaxFeelerLength);
            if (f.Length.getValue() < Settings.CreatureSettings.MinFeelerLength)
                f.Length.setValue(Settings.CreatureSettings.MinFeelerLength);

            Energy.setValue(
                    Energy.getValue() -
                            (Settings.CreatureSettings.EnergyDrainPerFeelerLengthPerSecond *
                             Math.pow(f.Length.getValue() - Settings.CreatureSettings.MinFeelerLength,
                                     Settings.CreatureSettings.EnergyDrainPerFeelerLengthExponent) * deltaTime / 1000));
        }

        if (Energy.getValue() <= 0)
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

    public double getRotationChange(){
        return RotationChange.getValue();
    }
    public void setRotationChange(double value){
        RotationChange.setValue(value);
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
        return Energy.getValue();
    }
    public void setEnergy(double value){
        Energy.setValue(value);
    }

    public double getAge() {
        return Age.getValue();
    }
    public void setAge(double age) {
        Age.setValue(age);
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
