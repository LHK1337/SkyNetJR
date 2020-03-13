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
    private List<NeuralProperty> Inputs;                // Eingänge des neuronalen Netzes (Das was die Kreatur spürt bzw. wie sie ihre Umgebung wahrnimmt.)
    private List<NeuralProperty> Outputs;               // Ausgänge des neuronalen Netzes (Kann man als Schnittstelle zu den Muskeln der Kreatur interpretieren.)
    private List<Integer> HiddenLayerNeurons;           // Liste mit den Anzahlen an Neuronen in den versteckten Schichten des Netzes
    private List<double[]> HiddenNeuronActivities;

    private final Object WeightsLock = new Object();
    private List<double[][]> Weights;                   // Gewichtsmatrizen: [outputCounts][inputCount]
                                                        // Eine Liste mit 2-dimensionalen Floatarrays, die die Wichtungen der Neuronen untereinander enthalten

    private NeuralProperty<Double> bias;                // Bias => Ein Grundsignal in einem neuronalen, welches immmer aktiv ist (hier gleich 1),
                                                        //         damit erzwinkt man, dass ungelernte Netze nicht nur Nullen ausgeben
                                                        // Erzwingt eine gewisse Grundaktivität

    private boolean Destroyed;                          // Gibt an, ob dieses Netz bereits zerstört, also nicht mehr in Verwendung ist.

    public NeuralNetwork() {
        // Grundwerte für neue Netze festlegen und Objekte initialisieren
        Destroyed = false;

        Weights = new ArrayList<>();

        bias = new NeuralProperty<Double>(1d, NeuralPropertyType.Bias);

        Inputs = new ArrayList<>();
        Inputs.add(bias);                               // Bias den Eingänge hinzufügen

        Outputs = new ArrayList<>();
        HiddenLayerNeurons = new ArrayList<>();
        HiddenNeuronActivities = new ArrayList<>();
    }

    // Konstruktor, um eine Kopie eines Netzes zu erstellen
    public NeuralNetwork(NeuralNetwork nn){
        this();

        // Gewichte des Parentalnetzes übernehmen
        synchronized (this.WeightsLock){
            for (int i = 0; i < nn.Weights.size(); i++) {
                Weights.add(nn.Weights.get(i).clone());
            }

            // Versteckte Schichten übernehmen
            this.HiddenLayerNeurons.addAll(nn.HiddenLayerNeurons);
        }
    }

    // Ermittelt Schicht im neuronalen Netz mit den meisten Neuronen
    private int getHighestNeuronCount() {
        int i = Inputs.size();

        if (Outputs.size() > i) i = Outputs.size();
        for (Integer n: HiddenLayerNeurons){
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
    public void Evaluate(){
        if (Destroyed) return;  // zerstörte Netze nicht mehr berechnen

        HiddenNeuronActivities.clear();
        double[][] v = new double[getHighestNeuronCount()][2];  // Array entspricht zwei Vektoren, die in der folgenden Matrixmultiplikation Sumnad und Produkt enthalten
                                                                // Vektor(mit Ergebnissen der letzten Schicht) x Gewichtsmatrix(dieser Schicht) = Vektor(Ergebnisse dieser Schicht)
                                                                // v1 x m = v2

        // Eingabe des Netzes als Ausgangswerte der ersten Schicht verwenden
        for (int i = 0; i < Inputs.size(); i++) {
            v[i][0] = (double) Inputs.get(i).getValue();
        }

        // Implementation der Matrixmultiplikation
        for (int i = 0; i < Weights.size(); i++) {
            double[][] layer = Weights.get(i);
            HiddenNeuronActivities.add(new double[layer.length]);

            for (int j = 0; j < layer.length; j++) {
                v[j][1] = 0;
                for (int k = 0; k < layer[j].length; k++) {
                    v[j][1] += v[k][0] * layer[j][k];
                }
            }

            for (int j = 0; j < layer.length; j++) {
                v[j][0] = ActivationFunction(v[j][1]);
                HiddenNeuronActivities.get(HiddenNeuronActivities.size() - 1)[j] = v[j][0];
            }
        }

        // Ergebnisse der letztem Schicht in die Ausgänge des neuronalen Netzes schreiben
        for (int j = 0; j < Outputs.size(); j++) {
            Outputs.get(j).setValue(v[j][0]);
        }
    }

    // Implementation der Mutation
    public void Mutate(double mutationRate){
        Random r = new Random();

        int i,j,k;

        i = r.nextInt(Weights.size());
        j = r.nextInt(Weights.get(i).length);
        k = r.nextInt(Weights.get(i)[j].length);

        // eine zufällige Wichtung einer zufälligen Schicht geringfügig zufällig ändern
        Weights.get(i)[j][k] += (r.nextDouble() * (2 * mutationRate)) - mutationRate;
    }

    // Eine neue versteckte Schicht dem Netz hinzufügen
    public void AddHiddenLayer(int neurons) throws IndexOutOfBoundsException {
        AddHiddenLayer(neurons, true);
    }
    public void AddHiddenLayer(int neurons, boolean rebuild) throws IndexOutOfBoundsException {
        if (neurons <= 0)
            throw new IndexOutOfBoundsException("Hidden layer neurons <= 0");

        HiddenLayerNeurons.add(neurons);

        if (rebuild) RebuildAllWeights();
    }

    //// Eine neue versteckte Schicht aus dem Netz entfernen
    // neuste Schicht entfernen
    public void RemoveLatestHiddenLayer() {
        if (HiddenLayerNeurons.size() > 1)
        {
            HiddenLayerNeurons.remove(HiddenLayerNeurons.size() - 1);

            RebuildAllWeights();
        }
    }
    // eine bestimmte Schicht entfernen
    public void RemoveRandomHiddenLayer() {
        if (HiddenLayerNeurons.size() > 1)
        {
            HiddenLayerNeurons.remove(new Random().nextInt(HiddenLayerNeurons.size()));

            RebuildAllWeights();
        }
    }

    // Fügt dem Netz eine Eingabe hinzu
    public void AddInput(NeuralProperty np){
        AddInput(np, true);
    }
    public void AddInput(NeuralProperty np, boolean rebuild){
        Inputs.add(np);
        if (rebuild) RebuildInputWeights();
    }

    // Fügt dem Netz eine Ausgabe hinzu
    public void AddOutput(NeuralProperty np){
        AddOutput(np, true);
    }
    public void AddOutput(NeuralProperty np, boolean rebuild){
        Outputs.add(np);
        if (rebuild) RebuildOutputWeights();
    }

    // Entfernt eine Eingabe aus dem Netz
    public void RemoveInput(NeuralProperty np){
        AddInput(np, true);
    }
    public void RemoveInput(NeuralProperty np, boolean rebuild){
        Inputs.remove(np);
        if (rebuild) RebuildInputWeights();
    }

    // Entfernt eine Ausgabe aus dem Netz
    public void RemoveOutput(NeuralProperty np){
        AddOutput(np, true);
    }
    public void RemoveOutput(NeuralProperty np, boolean rebuild){
        Outputs.remove(np);
        if (rebuild) RebuildOutputWeights();
    }

    // Ausgabe der Gewichte in der Konsole
    public void PrintWeights(){
        System.out.println("Inputs: " + Inputs.size());
        System.out.println("Outputs:" + Outputs.size());

        System.out.print("HiddenLayers: { ");
        for (int i = 0; i < HiddenLayerNeurons.size(); i++) {
            if (i != 0) System.out.print(", ");
            System.out.print(HiddenLayerNeurons.get(i));
        }
        System.out.println(" }");

        System.out.println("Layer: " + Weights.size() + " | Input + " + HiddenLayerNeurons.size() + " Hidden + Output");

        for (int i = 0; i < Weights.size(); i++) {
            System.out.println(Weights.get(i).length + "x" + Weights.get(i)[0].length);

            for (int j = 0; j < Weights.get(i).length; j++) {
                for (int k = 0; k < Weights.get(i)[j].length; k++) {
                    System.out.print(Weights.get(i)[j][k] + "\t");
                }
                System.out.println();
            }
        }

        System.out.println();
    }

    // Matrizen zwischen den Schichten erstellen
    private void RebuildAllWeights(){
        List<double[][]> newWeights = new ArrayList<>();

        if (HiddenLayerNeurons.size() == 0) newWeights.add(new double[Outputs.size()][Inputs.size()]);
        else {
            newWeights.add(new double[HiddenLayerNeurons.get(0)][Inputs.size()]);

            for (int i = 0; i < HiddenLayerNeurons.size() - 1; i++)
                newWeights.add(new double[HiddenLayerNeurons.get(i + 1)][HiddenLayerNeurons.get(i)]);

            newWeights.add(new double[Outputs.size()][HiddenLayerNeurons.get(HiddenLayerNeurons.size() - 1)]);
        }

        Random r = new Random();

        for (int i = 0; i < newWeights.size(); i++)
            if (i < Weights.size())
                for (int j = 0; j < newWeights.get(i).length; j++)
                    if (j < Weights.get(i).length)
                        for (int k = 0; k < newWeights.get(i)[j].length; k++)
                            if (k < Weights.get(i)[j].length)
                                newWeights.get(i)[j][k] = Weights.get(i)[j][k];
                            else
                                newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;
                    else
                        for (int k = 0; k < newWeights.get(i)[j].length; k++)
                            newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;
            else
                for (int j = 0; j < newWeights.get(i).length; j++)
                    for (int k = 0; k < newWeights.get(i)[j].length; k++) newWeights.get(i)[j][k] = (r.nextDouble() * 2 * Settings.CreatureSettings.MutationRates.InitialWeightRange) - Settings.CreatureSettings.MutationRates.InitialWeightRange;

        synchronized (WeightsLock){
            Weights.clear();
            Weights.addAll(newWeights);
        }
    }
    private void RebuildInputWeights(){
        RebuildAllWeights();
    }
    private void RebuildOutputWeights(){
        RebuildAllWeights();
    }

    // Getter Funktionen
    public NeuralProperty[] getInputs(){
        return Inputs.toArray(new NeuralProperty[0]);
    }
    public NeuralProperty[] getOutputs(){
        return Outputs.toArray(new NeuralProperty[0]);
    }

    public Object getWeightsLock() { return WeightsLock; }
    public List<double[][]> getWeights(){
        return Weights;
    }

    public int getHiddenLayerCount() {
        return HiddenLayerNeurons.size();
    }
    public List<double[]> getHiddenNeuronActivities(){
        return HiddenNeuronActivities;
    }

    // Zerstört das Netz und räumt den Arbeitspeicher auf
    public void Destroy() {
        Destroyed = true;

        Inputs.clear();
        Outputs.clear();
        HiddenLayerNeurons.clear();
        Weights.clear();
    }
}
