/*
* Klasse die ein Gehirn rendert.
* */

package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.AI.NeuralNetwork;
import SkyNetJR.Graphics.Rendering.Renderer;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BrainRenderer extends Renderer {
    // Voreinstellungen
    private static final int CirclePrecision = 20;
    private static final Vector3d LineColor = new Vector3d(1, 1, 1);
    private static final Vector3d NeuronPositiveActivityColor = new Vector3d(0, 1, 0);
    private static final Vector3d NeuronNegativeActivityColor = new Vector3d(1, 0, 0);
    private static final double NeuronRadius = 15;
    private static final double SpacingX = NeuronRadius * 10;
    private static final double SpacingY = NeuronRadius / 4;

    private NeuralNetwork _nn;

    public BrainRenderer(NeuralNetwork nn){
        _nn = nn;
    }

    @Override
    public void render(int offsetX, int offsetY) {
        if (_nn != null){
            List<double[][]> weights = _nn.getWeights();
            List<double[]> neuronActivities = new ArrayList<>();

            int maxNeuronCount = _nn.getInputs().length;

            // Wert pro Neuron berechnen
            neuronActivities.add(new double[_nn.getInputs().length]);
            for (int j = 0; j < neuronActivities.get(neuronActivities.size() - 1).length; j++) {
                neuronActivities.get(neuronActivities.size() - 1)[j] = (double) _nn.getInputs()[j].getValue();
            }

            neuronActivities.addAll(_nn.getHiddenNeuronActivities());

            for (int i = 0; i < weights.size(); i++) {
                double[][] layer = weights.get(i);

                // Größte Schicht des Neuronalen Netzes ermitteln
                if (layer.length > maxNeuronCount)
                    maxNeuronCount = layer.length;
            }

            // Position der Neuronen in dem Fenster berechnen
            List<Vector2d[]> neuronVertices = new ArrayList<>();
            for (int i = 0; i < neuronActivities.size(); i++) {
                neuronVertices.add(new Vector2d[neuronActivities.get(i).length]);

                for (int j = 0; j < neuronVertices.get(neuronVertices.size() - 1).length; j++) {
                    neuronVertices.get(neuronVertices.size() - 1)[j] = new Vector2d(
                            offsetX + NeuronRadius + (i * (SpacingX + 2 * NeuronRadius)),
                            offsetY + (NeuronRadius + (j * (SpacingY + 2 * NeuronRadius))) *
                                    ((double)maxNeuronCount / neuronVertices.get(neuronVertices.size() - 1).length) );
                }
            }

            // Neuronales Netz zeichen/rendern
            for (int i = 0; i < neuronVertices.size(); i++) {
                if (i < neuronVertices.size() - 1){
                    for (int j = 0; j < neuronVertices.get(i).length; j++) {
                        for (int k = 0; k < neuronVertices.get(i + 1).length; k++) {
                            double weight = weights.get(i)[k][j];

                            Vector3d color = weight > 0 ?
                                    new Vector3d(
                                        (weight * NeuronPositiveActivityColor.x),
                                        (weight * NeuronPositiveActivityColor.y),
                                        (weight * NeuronPositiveActivityColor.z)) :
                                    new Vector3d(
                                        (-1 * weight * NeuronNegativeActivityColor.x),
                                        (-1 * weight * NeuronNegativeActivityColor.y),
                                        (-1 * weight * NeuronNegativeActivityColor.z));


                            drawLine(
                                    new Vector2d(neuronVertices.get(i)[j].x + NeuronRadius, neuronVertices.get(i)[j].y),
                                    new Vector2d(neuronVertices.get(i + 1)[k].x - NeuronRadius, neuronVertices.get(i + 1)[k].y),
                                    color);
                        }
                    }
                }

                for (int j = 0; j < neuronVertices.get(i).length; j++) {
                    drawCircle(neuronVertices.get(i)[j].x, neuronVertices.get(i)[j].y, NeuronRadius,
                            neuronActivities.get(i)[j] > 0 ?
                                    new Vector3d(
                                            NeuronPositiveActivityColor.x * neuronActivities.get(i)[j],
                                            NeuronPositiveActivityColor.y * neuronActivities.get(i)[j],
                                            NeuronPositiveActivityColor.z * neuronActivities.get(i)[j]) :
                                    new Vector3d(
                                            NeuronNegativeActivityColor.x * -1 * neuronActivities.get(i)[j],
                                            NeuronNegativeActivityColor.y * -1 * neuronActivities.get(i)[j],
                                            NeuronNegativeActivityColor.z * -1 * neuronActivities.get(i)[j])
                            );
                    drawHollowCircle(neuronVertices.get(i)[j].x, neuronVertices.get(i)[j].y, NeuronRadius, LineColor);
                }
            }
        }
    }

    // Zeichet eine Linie zwischen p und q mit der Farbe color
    private void drawLine(Vector2d p, Vector2d q, Vector3d color){
        GL11.glColor3d(color.x, color.y, color.z);
        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex2d(p.x, p.y);
        GL11.glVertex2d(q.x, q.y);

        GL11.glEnd();
    }

    // Zeichet einen Kreis an der Position (x;y), mit dem Radius r und füllt ihn mit der Farbe fill
    private void drawCircle(double x, double y, double r, Vector3d fill){
        GL11.glColor3d(fill.x, fill.y, fill.z);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y); // Center
        for (int i = 0; i <= CirclePrecision; i++) {
            GL11.glVertex2d(
                    x + (r * Math.cos(i * 2*Math.PI / CirclePrecision)),
                    y + (r * Math.sin(i * 2*Math.PI / CirclePrecision))
            );
        }
        GL11.glEnd();
    }

    // Zeichet eine Kreislinie an der Position (x;y), mit dem Radius r und mit der Farbe color
    private void drawHollowCircle(double x, double y, double r, Vector3d color){
        GL11.glColor3d(color.x, color.y, color.z);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < CirclePrecision; i++) {
            GL11.glVertex2d(
                    x + (r * Math.cos(i * 2*Math.PI / CirclePrecision)),
                    y + (r * Math.sin(i * 2*Math.PI / CirclePrecision))
            );
        }
        GL11.glEnd();
    }

    // Getter und Setter
    public NeuralNetwork get_nn() {
        return _nn;
    }
    public void setNeuralNet(NeuralNetwork _nn) throws IllegalAccessException {
        this._nn = _nn;
    }

    @Override
    public void destroy() { }
}
