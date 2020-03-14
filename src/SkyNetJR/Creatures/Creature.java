/*
* Beschreibt die Logik der Kreaturen
* */

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
    // Eigenschaften der Kreatur
    private double _energy;
    private double _age;
    private double _energyOnCurrentTile;
    private boolean _currentTileWater;
    private long _generation;
    private double _positionX;
    private double _positionY;
    private double _rotation;
    private boolean _destroyed;
    private boolean _inhibit;

    //// Schnittstellen zwischen Eigenschaften und Gehirn der Kreatur
    // Sensing
    private NeuralProperty<Double> _neuralInEnergy;
    private NeuralProperty<Double> _neuralInAge;
    private NeuralProperty<Double> _neuralInEnergyOnCurrentTile;
    private NeuralProperty<Double> _neuralInCurrentTileWater;
    // Acting
    private NeuralProperty<Double> _neuralOutRotation;
    private NeuralProperty<Double> _neuralOutForward;
    private NeuralProperty<Double> _neuralOutEat;
    private NeuralProperty<Double> _neuralOutReplicate;

    // Auflistung der Fühler der Kreatur
    private List<Feeler> _feelers;

    // Genidentifikation der Kreatur (eine Farbe im RGB-Farbsystem)
    private Vector3d _genetics;

    // Spezieller Alterungskoeffizient der Zelle (beeinflusst den Alterungsprozess der Kreatur)
    private double _specificAgingFactor;

    // Referenzen
    private Population _population;  // aktuelle Population
    private NeuralNetwork _brain;    // Gehirn der Kreatur

    // Legt fest, ob die Kreatur gerendert werden soll
    private boolean _draw;

    //// Konstruktoren neuer Kreaturen
    // Rohe neue Kreatur
    private Creature() { }

    // Neue Kreatur einer Population an einer bestimmten Position
    public Creature(double positionX, double positionY, Population population) {
        this(positionX, positionY, 0, population);
    }

    // Neue Kreatur einer Population und Generation an einer bestimmten Position
    public Creature(double positionX, double positionY, long generation, Population population) {
        _population = population;
        _generation = generation;

        setDefaults();      // Standartwerte setzen
        initNewBrain();     // Neues Gehin initialisieren

        addFeeler();        // Ersten Fühler hinzufügen

        _positionX = positionX;
        _positionY = positionY;

        randomizeGenetics();    // Zufällige Genidentifikation generieren
    }

    // Neue Kreatur mit Vererbung einer Parentalkreatur
    public Creature(Creature parent){
        _inhibit = true;
        _generation = parent._generation + 1;
        _population = parent._population;
        setDefaults();              // Standartwerte setzen
        inheritFromParent(parent);  // Gehirn vererben

        _genetics = new Vector3d(parent._genetics.x, parent._genetics.y, parent._genetics.z);   // Genidentifikation übernehemn

        mutateGenetics();   // Genidentifikation mutieren
        mutateFeelers();    // Anzahl der Fühöer mutieren
        mutateBrain();      // Gehirn mutieren
    }

    // Eigenschaften einer Parentalgeneration vererben
    private void inheritFromParent(Creature parent){
        _energy = Settings.CreatureSettings.EnergyDrainPerReplication;
        _positionX = parent._positionX;
        _positionY = parent._positionY;

        _feelers = new ArrayList<>();
        for (int i = 0; i < parent._feelers.size(); i++) {
            _feelers.add(new Feeler((byte)i));

            _feelers.get(i).Angle.setValue(new Random().nextDouble() * 2 * Math.PI);
            _feelers.get(i).Length.setValue(0d);
        }

        _brain = new NeuralNetwork(parent._brain);
        NeuralProperty[] ins = parent.getBrain().getInputs();
        NeuralProperty[] outs = parent.getBrain().getOutputs();
        linkBrainFromParent(ins, outs);
    }

    // Eigenes Gehirn der Kreatur ähnlich der Parentalkreatur mit "Muskeln" verbinden
    private void linkBrainFromParent(NeuralProperty[] ins, NeuralProperty[] outs){
        for (NeuralProperty in : ins) {
            switch (in.getType()) { case Bias: break;
                case EnergySelf: _brain.addInput(_neuralInEnergy, false);break;
                case Age: _brain.addInput(_neuralInAge, false);break;
                case EnergyOnCurrentTile: _brain.addInput(_neuralInEnergyOnCurrentTile, false);break;
                case CurrentTileWater: _brain.addInput(_neuralInCurrentTileWater, false);break;

                // Feelers
                case FeelsWater: _brain.addInput(_feelers.get(in.getTag()).NeuralInFeelsWater, false);break;
                case EnergyValueFeeler: _brain.addInput(_feelers.get(in.getTag()).NeuralInEnergyValueFeeler, false);break;
            }
        }

        for (NeuralProperty out : outs) {
            switch (out.getType()) {
                case Rotate: _brain.addOutput(_neuralOutRotation, false); break;
                case Forward: _brain.addOutput(_neuralOutForward, false); break;
                case Eat: _brain.addOutput(_neuralOutEat, false); break;
                case Replicate: _brain.addOutput(_neuralOutReplicate, false); break;

                //Feelers
                case FeelerAngle: _brain.addOutput(_feelers.get(out.getTag()).Angle, false); break;
                case FeelerLength: _brain.addOutput(_feelers.get(out.getTag()).Length, false); break;
            }
        }
    }

    // Neues Gehirn initialisieren
    private void initNewBrain(){
        _brain = new NeuralNetwork();

        // Zufällige Anzahl an versteckten Schichten erstellen
        for (int i = 0; i < Settings.CreatureSettings.BaseHiddenNeuronLayers; i++) {
            _brain.addHiddenLayer(new Random().nextInt(Settings.CreatureSettings.MutationRates.MaxHiddenNeuronsPerLayer - Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer) + Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer, false);
        }

        // Eingänge und ausgänge verbinden
        _brain.addInput(_neuralInEnergy, false);
        _brain.addInput(_neuralInAge, false);
        if (Settings.CreatureSettings.CanFeelOnBody)
        {
            _brain.addInput(_neuralInEnergyOnCurrentTile, false);
            _brain.addInput(_neuralInCurrentTileWater, false);
        }

        _brain.addOutput(_neuralOutRotation, false);
        _brain.addOutput(_neuralOutForward, false);
        _brain.addOutput(_neuralOutEat, false);

        // beim letzten Eintrag rebuild = true, damit die Gewichtsmatrizen erneuert werden
        _brain.addOutput(_neuralOutReplicate, true);
    }

    // Standartwerte laden und Objekte initialisieren
    private void setDefaults(){
        _draw = true;

        _energy = Settings.CreatureSettings.BaseEnergy;
        _age = 0d;
        _energyOnCurrentTile = 0d;
        _currentTileWater = false;
        _rotation = new Random().nextDouble() * 2 * Math.PI;

        _neuralInEnergy = new NeuralProperty<Double>(_energy, NeuralPropertyType.EnergySelf);
        _neuralInAge = new NeuralProperty<Double>(_age, NeuralPropertyType.Age);
        if (Settings.CreatureSettings.CanFeelOnBody) {
            _neuralInEnergyOnCurrentTile = new NeuralProperty<>(NeuralPropertyType.EnergyOnCurrentTile);
            _neuralInCurrentTileWater = new NeuralProperty<>(NeuralPropertyType.CurrentTileWater);
        }

        _neuralOutRotation = new NeuralProperty<>(NeuralPropertyType.Rotate);
        _neuralOutForward = new NeuralProperty<>(NeuralPropertyType.Forward);
        _neuralOutEat = new NeuralProperty<>(NeuralPropertyType.Eat);
        _neuralOutReplicate = new NeuralProperty<>(NeuralPropertyType.Replicate);

        _feelers = new ArrayList<>();

        _specificAgingFactor = 1 + (new Random().nextDouble() * Settings.CreatureSettings.AgingVariance);
    }

    // Zufällige Genidentifikation generieren
    private void randomizeGenetics(){
        Random r = new Random();
        _genetics = new Vector3d(r.nextDouble(), r.nextDouble(), r.nextDouble());
    }

    // Genidentifikation mutieren
    private void mutateGenetics(){
        Random r = new Random();
        _genetics.x += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (_genetics.x > 1) _genetics.x = 1; else if (_genetics.x < 0) _genetics.x = 0;

        _genetics.y += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (_genetics.y > 1) _genetics.y = 1; else if (_genetics.y < 0) _genetics.y = 0;

        _genetics.z += (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.Genetics) - Settings.CreatureSettings.MutationRates.Genetics;
        if (_genetics.z > 1) _genetics.z = 1; else if (_genetics.z < 0) _genetics.z = 0;
    }

    // Fühler mutieren
    private void mutateFeelers(){
        Random r = new Random();

        if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.FeelerMutationChance) {
            if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.FeelerAddRemoveThreshold)
            {
                addFeeler();
                //System.out.println("[MUTATION] Feeler+");
            }else {
                if (_feelers.size() > 1){
                    removeFeeler();
                    //System.out.println("[MUTATION] Feeler-");
                }
            }
        }
    }

    // Gehirn mutieren
    private void mutateBrain(){
        Random r = new Random();

        if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.BrainMutationChance){
            if (r.nextDouble() >= Settings.CreatureSettings.MutationRates.HiddenLayerAddRemoveThreshold){
                _brain.addHiddenLayer(Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer + r.nextInt(Settings.CreatureSettings.MutationRates.MaxHiddenNeuronsPerLayer - Settings.CreatureSettings.MutationRates.MinHiddenNeuronsPerLayer));
                //System.out.println("[MUTATION] Hidden Layer+");
            }
            else
            {
                //brain.RemoveLatestHiddenLayer();
                _brain.removeRandomHiddenLayer();
                //System.out.println("[MUTATION] Hidden Layer-");
            }
        }

        _brain.mutate(Settings.CreatureSettings.MutationRates.Weights);
    }

    // Fühler hinzufügen
    private void addFeeler(){
        Feeler f = new Feeler((byte)_feelers.size());
        _feelers.add(f);

        f.Angle.setValue(new Random().nextDouble() * 2 * Math.PI);
        f.Length.setValue(Settings.CreatureSettings.InitialFeelerLength);

        linkFeeler(f);
    }

    // Fühler mit Gehirn verbinden
    private void linkFeeler(Feeler f){
        _brain.addInput(f.NeuralInFeelsWater, false);
        _brain.addInput(f.NeuralInEnergyValueFeeler, false);
        _brain.addOutput(f.Angle, false);
        _brain.addOutput(f.Length, true);
    }

    // Fühler entfernen
    private void removeFeeler(){
        Feeler f = _feelers.get(_feelers.size() - 1);
        _feelers.remove(f);

        _brain.removeInput(f.NeuralInFeelsWater, false);
        _brain.removeInput(f.NeuralInEnergyValueFeeler, false);
        _brain.removeOutput(f.Angle, false);
        _brain.removeOutput(f.Length, true);
    }

    // Umwelt der Kreatur wahrnehmen
    public void sense(){
        // Energy
        _neuralInEnergy.setValue((2 * _energy / Settings.CreatureSettings.BaseEnergy) - 1);

        // Age
        _neuralInAge.setValue(_age / 60 - 1);

        Tile t = _population.getTile((int)_positionX, (int)_positionY);

        if (Settings.CreatureSettings.CanFeelOnBody){
            // EnergyOnCurrentTile
            _energyOnCurrentTile = t.Energy;
            if (_neuralInEnergyOnCurrentTile != null)
                _neuralInEnergyOnCurrentTile.setValue((2 * _energyOnCurrentTile / Settings.SimulationSettings.MaxEnergyPerTile) - 1);

            // CurrentTileWater
            _currentTileWater = t.getType() == TileType.Water;
            if (_neuralInCurrentTileWater != null)
                _neuralInCurrentTileWater.setValue((_currentTileWater ? 1d : -1d));
        }

        // Feeler
        for (Feeler feeler : _feelers) {
            int feelsOnX = (int) (Math.round(_positionX + (Math.cos(feeler.Angle.getValue()) * feeler.Length.getValue()) / Settings.WorldSettings.TileSize));
            int feelsOnY = (int) (Math.round(_positionY + (Math.sin(feeler.Angle.getValue()) * feeler.Length.getValue()) / Settings.WorldSettings.TileSize));

            t = _population.getTile(feelsOnX, feelsOnY);

            // Feeler.FeelsWater
            feeler.NeuralInFeelsWater.setValue((t.getType() == TileType.Water ? 1d : -1d));

            // Feeler.EnergyValueFeeler
            feeler.NeuralInEnergyValueFeeler.setValue((2 * _energyOnCurrentTile / Settings.SimulationSettings.MaxEnergyPerTile) - 1);
        }
    }

    // Entsprechend der Ausgänge des Gehirns reagieren
    public void act(double deltaTime) {
        if (_inhibit) {
            _inhibit = false;
            return;
        }

        // Age
        _age += ((deltaTime / 1000));

        // Constant Energy Cost
        _energy -= (Settings.CreatureSettings.EnergyDrainPerSecond * deltaTime / 1000);
        _energy -= (Settings.CreatureSettings.AgeEnergyDrainPerSecond * _specificAgingFactor * (Math.pow(_age, 2)) * deltaTime / 1000);
        if (_currentTileWater)
            _energy -= (Settings.CreatureSettings.EnergyDrainOnWaterPerSecond * deltaTime / 1000);

        // Fat index
        // Je mehr Energie die Kreatur besitzt, desto mehr Energie verbraucht sie im Grundumsatz (vgl. Übergewicht)
        if (_energy > Settings.CreatureSettings.BaseEnergy){
            _energy -= ((1/Settings.CreatureSettings.AllowedFatness) * (_energy - Settings.CreatureSettings.BaseEnergy)) / 1000;
        }

        // Eat
        _energy += _population.Eat((int)_positionX, (int)_positionY, _neuralOutEat.getValue() * Settings.CreatureSettings.MaxEatPortionPerSecond * deltaTime / 1000);

        // Rotation
        _rotation += _neuralOutRotation.getValue() * Settings.CreatureSettings.RotationRangePerSecond / 1000;

        // Forward
        double move = Settings.CreatureSettings.MovingRangePerSecond * deltaTime / 1000;
        _energy -=  (Settings.CreatureSettings.MovingEnergyDrainPerPixel * Math.abs(_neuralOutForward.getValue()) * move);

        double moveX = Math.cos(_rotation) * (_neuralOutForward.getValue() * move);
        double moveY = Math.sin(_rotation) * (_neuralOutForward.getValue() * move);

        _positionX += moveX;
        _positionY += moveY;

        // Replication
        if (_age >= Settings.CreatureSettings.ReplicationMinAge){
            if (_neuralOutReplicate.getValue() > 0){
                _energy -= Settings.CreatureSettings.EnergyDrainPerReplication;

                if (_energy > 0)
                    replicate();
            }
        }

        // Feeler
        _energy -= (Settings.CreatureSettings.EnergyDrainPerFeelerPerSecond * _feelers.size() * deltaTime / 1000);

        for (Feeler f : _feelers){
            // Feeler.Angle
            f.Angle.setValue(f.Angle.getValue() * 2 * Math.PI + _neuralOutRotation.getValue());

            // Feeler.Length
            f.Length.setValue(f.Length.getValue() * Settings.CreatureSettings.MaxFeelerLength);
            if (f.Length.getValue() < Settings.CreatureSettings.MinFeelerLength)
                f.Length.setValue(Settings.CreatureSettings.MinFeelerLength);

            _energy -= (Settings.CreatureSettings.EnergyDrainPerFeelerLengthPerSecond *
                        Math.pow(f.Length.getValue() - Settings.CreatureSettings.MinFeelerLength,
                                Settings.CreatureSettings.EnergyDrainPerFeelerLengthExponent) * deltaTime / 1000);
        }

        if (_energy <= 0)
            Destroy();
    }

    // Kreatur replizieren
    private void replicate(){
        _population.AddCreature(new Creature(this));
    }

    // Getter und Setter
    public NeuralNetwork getBrain() { return _brain; }

    public double getRotation(){
        return _rotation;
    }
    public void setRotation(double value){
        _rotation = value;
    }

    public double getPositionY(){
        return _positionY;
    }
    public void setPositionY(double value){
        _positionY = value;
    }

    public double getPositionX(){
        return _positionX;
    }
    public void setPositionX(double value){
        _positionX = value;
    }

    public int getPositionYi(){ return (int)_positionY; }
    public void setPositionYi(int value){
        _positionY = value;
    }

    public int getPositionXi(){
        return (int)_positionX;
    }
    public void setPositionXi(int value){
        _positionX = value;
    }

    public double getEnergy(){
        return _energy;
    }
    public void setEnergy(double value){
        _energy = value;
    }

    public double getAge() {
        return _age;
    }
    public void setAge(double age) {
        _age = age;
    }

    public long getGeneration() {
        return _generation;
    }

    public Vector3d getGenetics() {
        return _genetics;
    }

    public List<Feeler> getFeelers() {
        return _feelers;
    }

    public boolean isDestroyed() {
        return _destroyed;
    }

    public boolean isInhibiting(){
        return _inhibit;
    }

    public boolean getDraw(){
        return _draw;
    }
    public void setDraw(boolean value){
        _draw = value;
    }

    // Kreatur zerstören und Arbeitspeicher aufräumen
    private void Destroy() {
        _brain.destroy();
        _feelers.clear();

        _population.RemoveCreature(this);

        _destroyed = true;
    }
}