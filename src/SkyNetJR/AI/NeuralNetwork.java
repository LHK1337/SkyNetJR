/*
* Neuronale Netze sind die Gehirne der Kreaturen.
* Diese Klasse enthält die Logik hinter nueronalen Netzen sowie deren Erstellung, Vererbung und Mutation.
* https://de.wikipedia.org/wiki/K%C3%BCnstliches_neuronales_Netz
* */

package SkyNetJR.AI;

import SkyNetJR.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private List<NeuralProperty> _inputs;                // Eingänge des neuronalen Netzes (Das was die Kreatur spürt bzw. wie sie ihre Umgebung wahrnimmt.)
    private List<NeuralProperty> _outputs;               // Ausgänge des neuronalen Netzes (Kann man als Schnittstelle zu den Muskeln der Kreatur interpretieren.)
    private List<Integer> _hiddenLayerNeurons;           // Liste mit den Anzahlen an Neuronen in den versteckten Schichten des Netzes
    private List<double[]> _hiddenNeuronActivities;

    private final Object _weightsLock = new Object();
    private List<double[][]> _weights;                   // Gewichtsmatrizen: [outputCounts][inputCount]
                                                        // Eine Liste mit 2-dimensionalen Floatarrays, die die Wichtungen der Neuronen untereinander enthalten

    private NeuralProperty<Double> _bias;                // Bias => Ein Grundsignal in einem neuronalen, welches immmer aktiv ist (hier gleich 1),
                                                        //         damit erzwinkt man, dass ungelernte Netze nicht nur Nullen ausgeben
                                                        // Erzwingt eine gewisse Grundaktivität

    private boolean _destroyed;                          // Gibt an, ob dieses Netz bereits zerstört, also nicht mehr in Verwendung ist.

    public NeuralNetwork() {
        // Grundwerte für neue Netze festlegen und Objekte initialisieren
        _destroyed = false;

        _weights = new ArrayList<>();

        _bias = new NeuralProperty<Double>(1d, NeuralPropertyType.Bias);

        _inputs = new ArrayList<>();
        _inputs.add(_bias);                               // Bias den Eingänge hinzufügen

        _outputs = new ArrayList<>();
        _hiddenLayerNeurons = new ArrayList<>();
        _hiddenNeuronActivities = new ArrayList<>();
    }

    // Konstruktor, um eine Kopie eines Netzes zu erstellen
    public NeuralNetwork(NeuralNetwork nn){
        this();

        // Gewichte des Parentalnetzes übernehmen
        synchronized (this._weightsLock){
            for (int i = 0; i < nn._weights.size(); i++) {
                _weights.add(nn._weights.get(i).clone());
            }

            // Versteckte Schichten übernehmen
            this._hiddenLayerNeurons.addAll(nn._hiddenLayerNeurons);
        }
    }

    // Ermittelt Schicht im neuronalen Netz mit den meisten Neuronen
    private int getHighestNeuronCount() {
        int i = _inputs.size();

        if (_outputs.size() > i) i = _outputs.size();
        for (Integer n: _hiddenLayerNeurons){
            if (n > i) i = n;
        }

        return i;
    }

    // Mathematische Aktivierungsfunktion der Neuronen
    public static double ActivationFunction(double x){
        return TangentHyperbolic(x);
        //return Sigmoid(x);
    }

    // Verfügbare Aktivierungsfunktionen
    private static double TangentHyperbolic(double x){ return Math.tanh(x); }               // Tangens hyperbolicus
    public static double Sigmoid(double x) { return (1/( 1 + Math.pow(Math.E,(-1*x)))); }   // Sigmoid Funktion

    // Funktion, die die aktuellen Ausgänge des Netzes berechnet
    public void evaluate(){
        if (_destroyed) return;  // zerstörte Netze nicht mehr berechnen

        _hiddenNeuronActivities.clear();
        double[][] v = new double[getHighestNeuronCount()][2];  // Array entspricht zwei Vektoren, die in der folgenden Matrixmultiplikation Sumnad und Produkt enthalten
                                                                // Vektor(mit Ergebnissen der letzten Schicht) x Gewichtsmatrix(dieser Schicht) = Vektor(Ergebnisse dieser Schicht)
                                                                // v1 x m = v2

        // Eingabe des Netzes als Ausgangswerte der ersten Schicht verwenden
        for (int i = 0; i < _inputs.size(); i++) {
            v[i][0] = (double) _inputs.get(i).getValue();
        }

        // Implementation der Matrixmultiplikation
        for (int i = 0; i < _weights.size(); i++) {
            double[][] layer = _weights.get(i);
            _hiddenNeuronActivities.add(new double[layer.length]);

            for (int j = 0; j < layer.length; j++) {
                v[j][1] = 0;
                for (int k = 0; k < layer[j].length; k++) {
                    v[j][1] += v[k][0] * layer[j][k];
                }
            }

            for (int j = 0; j < layer.length; j++) {
                v[j][0] = ActivationFunction(v[j][1]);
                _hiddenNeuronActivities.get(_hiddenNeuronActivities.size() - 1)[j] = v[j][0];
            }
        }

        // Ergebnisse der letztem Schicht in die Ausgänge des neuronalen Netzes schreiben
        for (int j = 0; j < _outputs.size(); j++) {
            _outputs.get(j).setValue(v[j][0]);
        }
    }

    // Implementation der Mutation
    public void mutate(double mutationRate){
        Random r = new Random();

        int i,j,k;

        i = r.nextInt(_weights.size());
        j = r.nextInt(_weights.get(i).length);
        k = r.nextInt(_weights.get(i)[j].length);

        // eine zufällige Wichtung einer zufälligen Schicht geringfügig zufällig ändern
        _weights.get(i)[j][k] += (r.nextDouble() * (2 * mutationRate)) - mutationRate;
    }

    // Eine neue versteckte Schicht dem Netz hinzufügen
    public void addHiddenLayer(int neurons) throws IndexOutOfBoundsException {
        addHiddenLayer(neurons, true);
    }
    public void addHiddenLayer(int neurons, boolean rebuild) throws IndexOutOfBoundsException {
        if (neurons <= 0)
            throw new IndexOutOfBoundsException("Hidden layer neurons <= 0");

        _hiddenLayerNeurons.add(neurons);

        if (rebuild) rebuildAllWeights();
    }

    //// Eine neue versteckte Schicht aus dem Netz entfernen
    // neuste Schicht entfernen
    public void RemoveLatestHiddenLayer() {
        if (_hiddenLayerNeurons.size() > 1)
        {
            _hiddenLayerNeurons.remove(_hiddenLayerNeurons.size() - 1);

            rebuildAllWeights();
        }
    }
    // eine bestimmte Schicht entfernen
    public void removeRandomHiddenLayer() {
        if (_hiddenLayerNeurons.size() > 1)
        {
            _hiddenLayerNeurons.remove(new Random().nextInt(_hiddenLayerNeurons.size()));

            rebuildAllWeights();
        }
    }

    // Fügt dem Netz eine Eingabe hinzu
    public void addInput(NeuralProperty np){
        addInput(np, true);
    }
    public void addInput(NeuralProperty np, boolean rebuild){
        _inputs.add(np);
        if (rebuild) rebuildInputWeights();
    }

    // Fügt dem Netz eine Ausgabe hinzu
    public void addOutput(NeuralProperty np){
        addOutput(np, true);
    }
    public void addOutput(NeuralProperty np, boolean rebuild){
        _outputs.add(np);
        if (rebuild) rebuildOutputWeights();
    }

    // Entfernt eine Eingabe aus dem Netz
    public void removeInput(NeuralProperty np){
        addInput(np, true);
    }
    public void removeInput(NeuralProperty np, boolean rebuild){
        _inputs.remove(np);
        if (rebuild) rebuildInputWeights();
    }

    // Entfernt eine Ausgabe aus dem Netz
    public void removeOutput(NeuralProperty np){
        addOutput(np, true);
    }
    public void removeOutput(NeuralProperty np, boolean rebuild){
        _outputs.remove(np);
        if (rebuild) rebuildOutputWeights();
    }

    // Ausgabe der Gewichte in der Konsole
    public void printWeights(){
        System.out.println("Inputs: " + _inputs.size());
        System.out.println("Outputs:" + _outputs.size());

        System.out.print("HiddenLayers: { ");
        for (int i = 0; i < _hiddenLayerNeurons.size(); i++) {
            if (i != 0) System.out.print(", ");
            System.out.print(_hiddenLayerNeurons.get(i));
        }
        System.out.println(" }");

        System.out.println("Layer: " + _weights.size() + " | Input + " + _hiddenLayerNeurons.size() + " Hidden + Output");

        for (int i = 0; i < _weights.size(); i++) {
            System.out.println(_weights.get(i).length + "x" + _weights.get(i)[0].length);

            for (int j = 0; j < _weights.get(i).length; j++) {
                for (int k = 0; k < _weights.get(i)[j].length; k++) {
                    System.out.print(_weights.get(i)[j][k] + "\t");
                }
                System.out.println();
            }
        }

        System.out.println();
    }

    // Matrizen zwischen den Schichten erstellen
    private void rebuildAllWeights(){
        List<double[][]> newWeights = new ArrayList<>();

        if (_hiddenLayerNeurons.size() == 0) newWeights.add(new double[_outputs.size()][_inputs.size()]);
        else {
            newWeights.add(new double[_hiddenLayerNeurons.get(0)][_inputs.size()]);

            for (int i = 0; i < _hiddenLayerNeurons.size() - 1; i++)
                newWeights.add(new double[_hiddenLayerNeurons.get(i + 1)][_hiddenLayerNeurons.get(i)]);

            newWeights.add(new double[_outputs.size()][_hiddenLayerNeurons.get(_hiddenLayerNeurons.size() - 1)]);
        }

        Random r = new Random();

        for (int i = 0; i < newWeights.size(); i++)
            if (i < _weights.size())
                for (int j = 0; j < newWeights.get(i).length; j++)
                    if (j < _weights.get(i).length)
                        for (int k = 0; k < newWeights.get(i)[j].length; k++)
                            if (k < _weights.get(i)[j].length)
                                newWeights.get(i)[j][k] = _weights.get(i)[j][k];
                            else
                                newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;
                    else
                        for (int k = 0; k < newWeights.get(i)[j].length; k++)
                            newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;
            else
                for (int j = 0; j < newWeights.get(i).length; j++)
                    for (int k = 0; k < newWeights.get(i)[j].length; k++) newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;

        synchronized (_weightsLock){
            _weights.clear();
            _weights.addAll(newWeights);
        }
    }
    private void rebuildInputWeights(){
        rebuildAllWeights();
    }
    private void rebuildOutputWeights(){
        rebuildAllWeights();
    }

    // Getter Funktionen
    public NeuralProperty[] getInputs(){
        return _inputs.toArray(new NeuralProperty[0]);
    }
    public NeuralProperty[] getOutputs(){
        return _outputs.toArray(new NeuralProperty[0]);
    }

    public Object getWeightsLock() { return _weightsLock; }
    public List<double[][]> getWeights(){
        return _weights;
    }

    public int getHiddenLayerCount() {
        return _hiddenLayerNeurons.size();
    }
    public List<double[]> getHiddenNeuronActivities(){
        return _hiddenNeuronActivities;
    }

    // Zerstört das Netz und räumt den Arbeitspeicher auf
    public void destroy() {
        _destroyed = true;

        _inputs.clear();
        _outputs.clear();
        _hiddenLayerNeurons.clear();
        _weights.clear();
    }
}
