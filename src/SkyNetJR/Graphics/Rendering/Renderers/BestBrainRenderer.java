/*
* Klasse die das Beste Gehirn einer Population rendert.
* */

package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.AI.NeuralNetwork;
import SkyNetJR.Creatures.Population;

public class BestBrainRenderer extends BrainRenderer {
    private Population _population;

    public BestBrainRenderer(Population population) {
        super(null);
        _population = population;
    }

    @Override
    public void render(int offsetX, int offsetY) {
        // Ermittlung des besten Gehirns
        if (_population != null && _population.isRunning() && _population.getCreatures().size() > 0) {
            try {
                super.setNeuralNet(_population.getCreatures().get(0).getBrain());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else return;

        super.render(offsetX, offsetY);
    }

    // Setter und Getter
    @Override
    public void setNeuralNet(NeuralNetwork nn) throws IllegalAccessException {
        throw new IllegalAccessException(); // Aufruf dieser Methode verbieten
    }

    public void setPopulation(Population p) {
        _population = p;
    }

    public Population getPopulation() {
        return _population;
    }
}
