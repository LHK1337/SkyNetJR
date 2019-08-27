package SkyNetJR.VirtualWorld;

import SkyNetJR.Rendering.Renderer;
import org.lwjgl.opengl.GL11;

public class VirtualWorldRenderer extends Renderer {
    private VirtualWorld world;

    public VirtualWorldRenderer(VirtualWorld world) {
        this.world = world;
    }

    @Override
    public void Render(int offsetX, int offsetY) {
        TileMap map = world.getTileMap();
        try {
            map.WaitForNextSwap();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        map.setReadyTilesInUse(true);
        Tile[][] t = map.getReadyTiles();

        for (int x = 0; x < t.length; x++)
            for (int y = 0; y < t[x].length; y++){

                if (t[x][y].getType() == TileType.Water)
                    GL11.glColor3d(0d, 0d, 1d);
                else {
                    GL11.glColor3d(0.0d, 0.0d + (t[x][y].Energy / map.getMaxEnergyPerTile()), 0d);
                }

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                GL11.glEnd();
            }

        map.setReadyTilesInUse(false);
    }

    public VirtualWorld getWorld() {
        return world;
    }

    public void setWorld(VirtualWorld world) {
        this.world = world;
    }
}
