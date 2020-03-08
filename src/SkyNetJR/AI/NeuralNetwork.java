package SkyNetJR.AI;

import SkyNetJR.Settings;
import SkyNetJR.Util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private List<NeuralProperty> Inputs;
    private List<NeuralProperty> Outputs;
    private List<Integer> HiddenLayerNeurons;
    private List<double[]> HiddenNeuronActivities;


    private final Object WeightsLock = new Object();
    private List<double[][]> Weights;   // Weight matrix: [outputCounts][inputCount]

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
        HiddenNeuronActivities = new ArrayList<>();
    }

    public NeuralNetwork(NeuralNetwork nn){
        this();

        synchronized (this.WeightsLock){
            for (int i = 0; i < nn.Weights.size(); i++) {
                Weights.add(nn.Weights.get(i).clone());
            }

            this.HiddenLayerNeurons.addAll(nn.HiddenLayerNeurons);
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

    public static double ActivationFunction(double x){
        return TangentHyperbolic(x);
    }

    private static double TangentHyperbolic(double x){ return Math.tanh(x); }

    public void EvaluateCpu(){
        if (Destroyed) return;

        HiddenNeuronActivities.clear();
        double[][] v = new double[getHighestNeuronCount()][2];

        for (int i = 0; i < Inputs.size(); i++) {
            v[i][0] = (double) Inputs.get(i).getValue();
        }

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

        for (int j = 0; j < Outputs.size(); j++) {
            Outputs.get(j).setValue(v[j][0]);
        }
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
        if (neurons <= 0)
            throw new IndexOutOfBoundsException("Hidden layer neurons <= 0");

        HiddenLayerNeurons.add(neurons);

        if (rebuild) RebuildAllWeights();
    }

    public void RemoveLatestHiddenLayer() {
        if (HiddenLayerNeurons.size() > 1)
        {
            HiddenLayerNeurons.remove(HiddenLayerNeurons.size() - 1);

            RebuildAllWeights();
        }
    }
    public void RemoveRandomHiddenLayer() {
        if (HiddenLayerNeurons.size() > 1)
        {
            HiddenLayerNeurons.remove(new Random().nextInt(HiddenLayerNeurons.size()));

            RebuildAllWeights();
        }
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
    public List<double[][]> getWeights(){
        return Weights;
    }

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

    public List<double[]> getHiddenNeuronActivities(){
        return HiddenNeuronActivities;
    }

    public void Destroy() {
        Destroyed = true;

        Inputs.clear();
        Outputs.clear();
        HiddenLayerNeurons.clear();
        Weights.clear();
    }

    public byte[] serialize() {
        List<Byte> bytes = new ArrayList<>();

        byte[] intBytes = new byte[Integer.BYTES * (3 + 2 * (Inputs.size() + Outputs.size()) + HiddenLayerNeurons.size() + 1 + 2 * Weights.size()) + Double.BYTES * (Inputs.size() + Outputs.size())];
        ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);

        int totalDoubles = 0;
        for (double[][] ddd : Weights) {
            totalDoubles += ddd.length * ddd[0].length;
        }

        byte[] doubleBytes = new byte[Double.BYTES * totalDoubles];
        ByteBuffer doubleBuffer = ByteBuffer.wrap(doubleBytes);

        byteBuffer.putInt(Inputs.size());
        byteBuffer.putInt(Outputs.size());
        byteBuffer.putInt(HiddenLayerNeurons.size());

        for (NeuralProperty<Double> p : Inputs){
            byteBuffer.put(p.getType().getValue());
            byteBuffer.put(p.getTag());
            byteBuffer.putDouble(p.getValue() == null ? 0 : p.getValue());

        }

        for (NeuralProperty<Double> p : Outputs){
            byteBuffer.put(p.getType().getValue());
            byteBuffer.put(p.getTag());
            byteBuffer.putDouble(p.getValue() == null ? 0 : p.getValue());
        }

        for (int i : HiddenLayerNeurons) byteBuffer.putInt(i);

        byteBuffer.putInt(Weights.size());

        for (double[][] ddd : Weights) {
            byteBuffer.putInt(ddd.length);
            byteBuffer.putInt(ddd[0].length);

            for (double[] dd : ddd){
                for (double d : dd)
                    doubleBuffer.putDouble(d);
            }
        }

        for (byte b : intBytes) bytes.add(b);
        for (byte b : doubleBytes) bytes.add(b);

        return Util.ByteListToByteArray(bytes);
    }

    private NeuralNetwork(byte[] bytes){
        Destroyed = false;
        Inputs = new ArrayList<>();
        Outputs = new ArrayList<>();
        Weights = new ArrayList<>();
        HiddenLayerNeurons = new ArrayList<>();
        HiddenNeuronActivities = new ArrayList<>();

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        int inputCount = byteBuffer.getInt();
        int outputCount = byteBuffer.getInt();
        int hiddenLayerNeuronsCount = byteBuffer.getInt();

        for (int i = 0; i < inputCount * 2; i += 2) {
            Inputs.add(new NeuralProperty(NeuralPropertyType.FromByte(byteBuffer.get()), byteBuffer.get()));
            Inputs.get(Inputs.size() - 1).setValue(byteBuffer.getDouble());
        }

        for (int i = 0; i < outputCount * 2; i += 2) {
            Outputs.add(new NeuralProperty(NeuralPropertyType.FromByte(byteBuffer.get()), byteBuffer.get()));
            Outputs.get(Outputs.size() - 1).setValue(byteBuffer.getDouble());
        }

        for (int i = 0; i < hiddenLayerNeuronsCount; i++) {
            HiddenLayerNeurons.add(byteBuffer.getInt());
        }

        int weightMatrices = byteBuffer.getInt();
        for (int i = 0; i < weightMatrices; i++) {
            int x = byteBuffer.getInt();
            int y = byteBuffer.getInt();

            Weights.add(new double[x][]);
            double[][] w = Weights.get(Weights.size() - 1);
            for (int i1 = 0, wLength = w.length; i1 < wLength; i1++) {
                w[i1] = new double[y];
            }
        }

        for (double[][] weight : Weights) {
            for (int i = 0; i < weight.length; i++) {
                for (int j = 0; j < weight[i].length; j++) {
                    weight[i][j] = byteBuffer.getDouble();
                }
            }
        }
    }

    public static NeuralNetwork Deserialize(byte[] bytes){
        return new NeuralNetwork(bytes);
    }
}
