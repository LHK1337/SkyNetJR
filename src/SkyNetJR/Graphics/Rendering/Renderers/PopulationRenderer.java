/*
* Klasse zum Rendern der Population
* */

package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.Creatures.Creature;
import SkyNetJR.Creatures.Feeler;
import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.Rendering.Renderer;
import SkyNetJR.Settings;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class PopulationRenderer extends Renderer {
    private Population _population;

    public PopulationRenderer(Population population) {
        this._population = population;
    }

    @Override
    public void render(int offsetX, int offsetY) {
        List<Creature> cs = _population.getCreatures();

        for (int i = 0; i < cs.size(); i++) {
            Creature c = cs.get(i);

            if (c.isDestroyed()){
                i--; continue;
            }

            if (c.isInhibiting() || !c.getDraw()) continue;

            // Körper der Kreatur zeichnen
            GL11.glColor3d(c.getGenetics().x, c.getGenetics().y, c.getGenetics().z);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(offsetX + c.getPositionX() - (Settings.CreatureSettings.CreatureSize * 0.5), offsetY + c.getPositionY() + (Settings.CreatureSettings.CreatureSize * 0.5));
            GL11.glVertex2d(offsetX + c.getPositionX() + (Settings.CreatureSettings.CreatureSize * 0.5), offsetY + c.getPositionY() + (Settings.CreatureSettings.CreatureSize * 0.5));
            GL11.glVertex2d(offsetX + c.getPositionX() + (Settings.CreatureSettings.CreatureSize * 0.5), offsetY + c.getPositionY() - (Settings.CreatureSettings.CreatureSize * 0.5));
            GL11.glVertex2d(offsetX + c.getPositionX() - (Settings.CreatureSettings.CreatureSize * 0.5), offsetY + c.getPositionY() - (Settings.CreatureSettings.CreatureSize * 0.5));
            GL11.glEnd();

            // Fühler der Kreatur zeichnen
            GL11.glColor3d(0, 0, 0);
            GL11.glBegin(GL11.GL_LINES);
            for (int j = 0; j < c.getFeelers().size(); j++) {
                if (j >= c.getFeelers().size()) break;
                Feeler f = c.getFeelers().get(j);
                GL11.glVertex2d(offsetX + c.getPositionX(), offsetY + c.getPositionY());
                GL11.glVertex2d(offsetX + c.getPositionX() + Math.cos(f.Angle.getValue()) * f.Length.getValue(),
                                offsetY + c.getPositionY() + Math.sin(f.Angle.getValue()) * f.Length.getValue());

            }
            GL11.glEnd();

            if (c.isDestroyed()){
                i--;
            }
        }
    }

    // Getter und Setter
    public Population getPopulation() {
        return _population;
    }
    public void setPopulation(Population population) {
        this._population = population;
    }

    @Override
    public void destroy() { }
}
