package SkyNetJR.AI;

import SkyNetJR.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private List<NeuralProperty> Inputs;
    private List<NeuralProperty> Outputs;
    private List<Integer> HiddenLayerNeurons;

    // Weight matrix: [inputCount][outputCounts]

    private final Object WeightsLock = new Object();
    private List<double[][]> Weights;

    private NeuralProperty<Double> bias;

    private boolean Destroyed;

    public NeuralNetwork() {
        Destroyed = false;

        Weights = new ArrayList<>();

        bias = new NeuralProperty<Double>(1d, NeuralPropertyType.Bias);

        Inputs = new ArrayList<>();
        Inputs.add(bias);

        Outputs = new ArrayList<>();
        HiddenLayerNeurons = new ArrayList<>();
    }

    public NeuralNetwork(NeuralNetwork nn){
        this();

        synchronized (this.WeightsLock){
            for (int i = 0; i < nn.Weights.size(); i++) {
                Weights.add(nn.Weights.get(i).clone());
            }
        }
    }

    private int getHighestNeuronCount() {
        int i = Inputs.size();

        if (Outputs.size() > i) i = Outputs.size();
        for (Integer n: HiddenLayerNeurons){
            if (n > i) i = n;
        }

        return i;
    }

    private static double ActivationFunction(double x){
        //return Sigmoid(x);
        return TangentHyperbolic(x);
    }
    
    private static double Sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
    }

    private static double TangentHyperbolic(double x){ return Math.tanh(x); }

    public void EvaluateCpu(){
        if (Destroyed) return;

        double[][] v = new double[getHighestNeuronCount()][2];

        for (int i = 0; i < Inputs.size(); i++) {
            v[i][0] = (double) Inputs.get(i).getValue();
        }

        int i = 0;
        for (; i < Weights.size(); i++) {
            double[][] matrix = Weights.get(i);

            for (int j = 0; j < matrix.length; j++) {
                for (int k = 0; k < matrix[j].length; k++) {
                    if (k == 0) v[j][1] = matrix[j][k] * v[k][0];
                    else v[j][1] += matrix[j][k] * v[k][0];
                }
            }

            for (int j = 0; j < v.length; j++) {
                v[j][0] = ActivationFunction(v[j][1]);  // Activation Function
            }
        }

        for (int j = 0; j < Outputs.size(); j++) {
            Outputs.get(j).setValue(v[j][0]);
        }
    }

    public void EvaluateGpu() {
        if (Destroyed) return;
    }

    public void Mutate(double mutationRate){
        Random r = new Random();

        int i,j,k;

        i = r.nextInt(Weights.size());
        j = r.nextInt(Weights.get(i).length);
        k = r.nextInt(Weights.get(i)[j].length);

        Weights.get(i)[j][k] += (r.nextDouble() * (2 * mutationRate)) - mutationRate;
    }

    public void AddHiddenLayer(int neurons) throws IndexOutOfBoundsException {
        AddHiddenLayer(neurons, true);
    }
    public void AddHiddenLayer(int neurons, boolean rebuild) throws IndexOutOfBoundsException {
        if (neurons <= 0) throw new IndexOutOfBoundsException("Hidden layer neurons <= 0");

        HiddenLayerNeurons.add(neurons);

        if (rebuild) RebuildAllWeights();
    }

    public void RemoveLatestHiddenLayer() {
        HiddenLayerNeurons.remove(HiddenLayerNeurons.size() - 1);

        RebuildAllWeights();
    }

    public void AddInput(NeuralProperty np){
        AddInput(np, true);
    }
    public void AddInput(NeuralProperty np, boolean rebuild){
        Inputs.add(np);
        if (rebuild) RebuildInputWeights();
    }

    public void AddOutput(NeuralProperty np){
        AddOutput(np, true);
    }
    public void AddOutput(NeuralProperty np, boolean rebuild){
        Outputs.add(np);
        if (rebuild) RebuildOutputWeights();
    }

    public void RemoveInput(NeuralProperty np){
        AddInput(np, true);
    }
    public void RemoveInput(NeuralProperty np, boolean rebuild){
        Inputs.remove(np);
        if (rebuild) RebuildInputWeights();
    }

    public void RemoveOutput(NeuralProperty np){
        AddOutput(np, true);
    }
    public void RemoveOutput(NeuralProperty np, boolean rebuild){
        Outputs.remove(np);
        if (rebuild) RebuildOutputWeights();
    }

    public NeuralProperty[] getInputs(){
        return Inputs.toArray(new NeuralProperty[0]);
    }

    public NeuralProperty[] getOutputs(){
        return Outputs.toArray(new NeuralProperty[0]);
    }

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

    public Object getWeightsLock() { return WeightsLock; }

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

    public int getHiddenLayerCount() {
        return HiddenLayerNeurons.size();
    }

    public void Destroy() {
        Destroyed = true;

        Inputs.clear();
        Outputs.clear();
        HiddenLayerNeurons.clear();
        Weights.clear();
    }
}
