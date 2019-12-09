package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.Graphics.Rendering.Renderer;
import SkyNetJR.Utils.ValueNoise2D;
import org.lwjgl.opengl.GL11;

public class NoiseRenderer extends Renderer {

    private final ValueNoise2D noise;

    public NoiseRenderer(ValueNoise2D noise) {
        this.noise = noise;
    }

    @Override
    public void Render(int offsetX, int offsetY) {
        GL11.glBegin(GL11.GL_POINTS);

        for (int x = 0; x < noise.WIDTH; x++) {
            for (int y = 0; y < noise.HEIGHT; y++) {
                GL11.glColor3d(noise.getHeightMap()[x][y], noise.getHeightMap()[x][y], noise.getHeightMap()[x][y]);
                GL11.glVertex2i(offsetX + x, offsetY + y);
            }
        }

        GL11.glEnd();
    }

    @Override
    public void Destroy() {
        // nothing to do
    }
}
