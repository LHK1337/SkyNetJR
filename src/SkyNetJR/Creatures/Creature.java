package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralNetwork;
import SkyNetJR.AI.NeuralProperty;
import SkyNetJR.AI.NeuralPropertyType;
import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Creature {
    private double Energy;
    private double Age;
    private double EnergyOnCurrentTile;
    private boolean CurrentTileWater;
    private boolean WasAttacked;
    private boolean WasHealed;

    // Sensing
    private NeuralProperty<Double> NeuralInEnergy;
    private NeuralProperty<Double> NeuralInAge;
    private NeuralProperty<Double> NeuralInEnergyOnCurrentTile;
    private NeuralProperty<Double> NeuralInCurrentTileWater;
    private NeuralProperty<Double> NeuralInWasAttacked;
    private NeuralProperty<Double> NeuralInWasHealed;

    // Acting
    private NeuralProperty<Double> NeuralOutRotation;
    private NeuralProperty<Double> NeuralOutForward;
    private NeuralProperty<Double> NeuralOutEat;
    private NeuralProperty<Double> NeuralOutReplicate;

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

    private boolean _draw;

    public boolean getDraw(){
        return _draw;
    }
    public void setDraw(boolean value){
        _draw = value;
    }

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
        for (NeuralProperty in : ins) {
            switch (in.getType()) { case Bias: break;
                case EnergySelf: brain.AddInput(NeuralInEnergy, false);break;
                case Age: brain.AddInput(NeuralInAge, false);break;
                case EnergyOnCurrentTile: brain.AddInput(NeuralInEnergyOnCurrentTile, false);break;
                case CurrentTileWater: brain.AddInput(NeuralInCurrentTileWater, false);break;
                case WasAttacked: brain.AddInput(NeuralInWasAttacked, false);break;
                case WasHealed: brain.AddInput(NeuralInWasHealed, false);break;

                // Feelers
                case FeelsWater: brain.AddInput(Feelers.get(in.getTag()).NeuralInFeelsWater, false);break;
                case EnergyValueFeeler: brain.AddInput(Feelers.get(in.getTag()).NeuralInEnergyValueFeeler, false);break;
                case FeelsCreature: brain.AddInput(Feelers.get(in.getTag()).NeuralInFeelsCreature, false);break;
                case GeneticDifference: brain.AddInput(Feelers.get(in.getTag()).NeuralInGeneticDifference, false);break;
                case OtherCreatureAge: brain.AddInput(Feelers.get(in.getTag()).NeuralInOtherCreatureAge, false);break;
                case OtherCreatureEnergy: brain.AddInput(Feelers.get(in.getTag()).NeuralInOtherCreatureEnergy, false);break;
            }
        }

        NeuralProperty[] outs = parent.getBrain().getOutputs();
        for (NeuralProperty out : outs) {
            switch (out.getType()) {
                case Rotate: brain.AddOutput(NeuralOutRotation, false); break;
                case Forward: brain.AddOutput(NeuralOutForward, false); break;
                case Eat: brain.AddOutput(NeuralOutEat, false); break;
                case Replicate: brain.AddOutput(NeuralOutReplicate, false); break;

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

        brain.AddInput(NeuralInEnergy, false);
        brain.AddInput(NeuralInAge, false);
        if (Settings.CreatureSettings.CanFeelOnBody)
        {
            brain.AddInput(NeuralInEnergyOnCurrentTile, false);
            brain.AddInput(NeuralInCurrentTileWater, false);
        }
        brain.AddInput(NeuralInWasAttacked, false);
        brain.AddInput(NeuralInWasHealed, false);

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
        WasAttacked = false;
        WasHealed = false;

        NeuralInEnergy = new NeuralProperty<Double>(Settings.CreatureSettings.BaseEnergy, NeuralPropertyType.EnergySelf);
        NeuralInAge = new NeuralProperty<Double>(0d, NeuralPropertyType.Age);
        NeuralInEnergyOnCurrentTile = new NeuralProperty<>(NeuralPropertyType.EnergyOnCurrentTile);
        NeuralInCurrentTileWater = new NeuralProperty<>(NeuralPropertyType.CurrentTileWater);
        NeuralInWasAttacked = new NeuralProperty<>(NeuralPropertyType.WasAttacked);
        NeuralInWasHealed = new NeuralProperty<>(NeuralPropertyType.WasHealed);

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
        brain.AddInput(f.NeuralInFeelsCreature, false);
        brain.AddInput(f.NeuralInGeneticDifference, false);
        brain.AddInput(f.NeuralInOtherCreatureAge, false);
        brain.AddInput(f.NeuralInOtherCreatureEnergy, false);
        brain.AddOutput(f.Angle, false);
        brain.AddOutput(f.Length, false);
        brain.AddOutput(f.Attack, false);
        brain.AddOutput(f.Heal, true);
    }

    private void RemoveFeeler(){
        Feeler f = Feelers.get(Feelers.size() - 1);
        Feelers.remove(f);

        brain.RemoveInput(f.NeuralInFeelsWater, false);
        brain.RemoveInput(f.NeuralInEnergyValueFeeler, false);
        brain.RemoveInput(f.NeuralInFeelsCreature, false);
        brain.RemoveInput(f.NeuralInGeneticDifference, false);
        brain.RemoveInput(f.NeuralInOtherCreatureAge, false);
        brain.RemoveInput(f.NeuralInOtherCreatureEnergy, false);
        brain.RemoveOutput(f.Angle, false);
        brain.RemoveOutput(f.Length, false);
        brain.RemoveOutput(f.Attack, false);
        brain.RemoveOutput(f.Heal, true);
    }

    public void Sense(){
        // Energy
        NeuralInEnergy.setValue(Energy / Settings.CreatureSettings.BaseEnergy);

        // Age
        NeuralInAge.setValue(Age / 10);

        Tile t = Population.getTile((int)PositionX, (int)PositionY);

        // EnergyOnCurrentTile
        EnergyOnCurrentTile = t.Energy;
        NeuralInEnergyOnCurrentTile.setValue(EnergyOnCurrentTile / Settings.SimulationSettings.MaxEnergyPerTile);

        // CurrentTileWater
        CurrentTileWater = t.getType() == TileType.Water;
        NeuralInCurrentTileWater.setValue((CurrentTileWater ? 1d : 0d));

        // TODO WasAttacked
        WasAttacked = false;
        NeuralInWasAttacked.setValue((WasAttacked ? 1d : 0d));

        // TODO WasHealed
        WasHealed = false;
        NeuralInWasHealed.setValue((WasHealed ? 1d : 0d));

        // Feeler
        for (Feeler feeler : Feelers) {

            //TODO FIX Collision, feelsOn, etc

            int feelsOnX = (int) (Math.round(PositionX + Math.cos(feeler.Angle.getValue()) / feeler.Length.getValue()));
            int feelsOnY = (int) (Math.round(PositionY + Math.sin(feeler.Angle.getValue()) / feeler.Length.getValue()));

            t = Population.getTile(feelsOnX, feelsOnY);

            // Feeler.FeelsWater
            feeler.NeuralInFeelsWater.setValue((t.getType() == TileType.Water ? 1d : 0d));

            // Feeler.EnergyValueFeeler
            feeler.NeuralInEnergyValueFeeler.setValue(t.Energy / Settings.SimulationSettings.MaxEnergyPerTile);

            Creature c = Population.getCollidingCreature(t.X, t.Y, this);

            // Feeler.FeelsCreature
            feeler.NeuralInFeelsCreature.setValue((c == null ? 0d : 1d));

            // Feeler.GeneticDifference
            if (c != null && c != this)
            {
                VirtualWorld.Current.setDraw(false);

                for (Creature creature: Population.getCreatures())
                    if (creature != this && creature != c)
                        creature._draw = false;

                System.out.println("[Detected Collision]");
                try {
                    Thread.sleep(10000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final double MaxGeneticDifference = Math.sqrt(3);

                Vector3d otherGenetics = c.getGenetics();
                double difference = Vector3d.distance
                        (this.Genetics.x, this.Genetics.y, this.Genetics.z,
                         otherGenetics.x, otherGenetics.y, otherGenetics.z);

                feeler.NeuralInGeneticDifference.setValue(difference / MaxGeneticDifference);
            } else feeler.NeuralInGeneticDifference.setValue(0d);

            // Feeler.OtherCreatureAge
            feeler.NeuralInOtherCreatureAge.setValue(c != null ? c.Age / 10 : 0d);

            // Feeler.OtherCreatureEnergy
            feeler.NeuralInOtherCreatureEnergy.setValue(c != null ? c.Energy / Settings.CreatureSettings.BaseEnergy : 0d);
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
        Energy -= (Settings.CreatureSettings.EnergyDrainPerSecond * deltaTime / 1000);
        Energy -= (Settings.CreatureSettings.AgeEnergyDrainPerSecond * SpecificAgingFactor * Age * deltaTime / 1000);
        if (CurrentTileWater)
            Energy -= (Settings.CreatureSettings.EnergyDrainOnWaterPerSecond * deltaTime / 1000);

        // Eat
        Energy += Population.Eat((int)PositionX, (int)PositionY, NeuralOutEat.getValue() * Settings.CreatureSettings.MaxEatPortionPerSecond * deltaTime / 1000);

        // Rotation
        //Rotation += NeuralOutRotation.getValue() * Settings.CreatureSettings.RotationRangePerSecond * deltaTime / 1000;
        Rotation = NeuralOutRotation.getValue() * 2 * Math.PI;

        // Forward
        double move = Settings.CreatureSettings.MovingRangePerSecond * deltaTime / 1000;
        Energy -=  (Settings.CreatureSettings.MovingEnergyDrainPerPixel * Math.abs(NeuralOutForward.getValue()) * move);

        double moveX = Math.cos(NeuralOutRotation.getValue()) * (NeuralOutForward.getValue() * move);
        double moveY = Math.sin(NeuralOutRotation.getValue()) * (NeuralOutForward.getValue() * move);

        PositionX += moveX;
        PositionY += moveY;

        Population.UpdateCollisionGrid(this, (int) PositionX, (int) PositionY);

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
