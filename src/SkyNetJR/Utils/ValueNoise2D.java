package SkyNetJR.Utils;

import SkyNetJR.VirtualWorld.GenerationInfo;

import java.util.List;
import java.util.Random;

public class ValueNoise2D {
    public final int WIDTH;
    public final int HEIGHT;

    public int Seed;
    public double Alpha;
    public int Octaves;
    public int StartFrequencyX;
    public int StartFrequencyY;

    public double[][] getHeightMap() {
        return heightMap;
    }

    private double[][] heightMap;
    private Random random;

    public ValueNoise2D(int width, int height, GenerationInfo gI) {
        WIDTH = width;
        HEIGHT = height;
        Alpha = 1;
        Seed = gI.Seed;
        Octaves = gI.Octaves;
        StartFrequencyX = gI.StartFrequencyX;
        StartFrequencyY = gI.StartFrequencyY;

        heightMap = new double[width][height];
        random = new Random(Seed);
    }
    public ValueNoise2D(int width, int height, int octaves, int startFrequencyX, int startFrequencyY) {
        WIDTH = width;
        HEIGHT = height;
        Seed = new Random().nextInt();
        Alpha = 1;
        Octaves = octaves;
        StartFrequencyX = startFrequencyX;
        StartFrequencyY = startFrequencyY;

        heightMap = new double[width][height];
        random = new Random(Seed);
    }

    public void Calculate(){
        int currentFreqX = StartFrequencyX;
        int currentFreqY = StartFrequencyY;

        double currentAlpha = Alpha;

        for (int oc = 0; oc < Octaves; oc++){
            if (oc > 0){
                currentFreqX *= 2;
                currentFreqY *= 2;
                currentAlpha /= 2;
            }

            double[][] discretePoints = new double[currentFreqX + 1][currentFreqY + 1];

            for (int i = 0; i < currentFreqX + 1; i++) {
                for (int j = 0; j < currentFreqY + 1; j++) {
                    discretePoints[i][j] = (random.nextDouble() * currentAlpha * 2) - currentAlpha;
                }
            }

            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++){
                    double currentX = (double)i /(double)WIDTH * (double)currentFreqX;
                    double currentY = (double)j /(double)HEIGHT * (double)currentFreqY;

                    int indexX = (int)currentX;
                    int indexY = (int)currentY;

                    double w0 = interpolate(discretePoints[indexX][indexY], discretePoints[indexX + 1][indexY], currentX - indexX);
                    double w1 = interpolate(discretePoints[indexX][indexY + 1], discretePoints[indexX + 1][indexY + 1], currentX - indexX);
                    heightMap[i][j] += interpolate(w0, w1, currentY - indexY);
                }
            }
        }

        normalize();
    }

    private void normalize(){
        double min = Double.MAX_VALUE;

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (heightMap[i][j] < min)
                    min = heightMap[i][j];
            }
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                heightMap[i][j] -= min;
            }
        }

        double max = Double.MIN_VALUE;

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (heightMap[i][j] > max)
                    max = heightMap[i][j];
            }
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                heightMap[i][j] /= max;
            }
        }
    }

    private double interpolate(double a, double b, double t){
        return CosineInterpolate(a, b, t);
    }

    private static double CosineInterpolate(double a,double b,double t) {
        double t2;
        t2 = (1.0d - Math.cos(t * Math.PI)) / 2.0d;
        return (a * (1.0d - t2) + b * t2);
    }

    private static double LinearInterpolate(double a,double b,double t) {
        return (a * (1 - t) + b * t);
    }
}
