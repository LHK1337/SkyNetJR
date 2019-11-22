package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.Graphics.Rendering.Renderer;
import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;
import org.lwjgl.opengl.GL11;

public class VirtualWorldRenderer extends Renderer {
    private VirtualWorld world;

    public VirtualWorldRenderer(VirtualWorld world) {
        this.world = world;
    }

    @Override
    public void Render(int offsetX, int offsetY) {
        TileMap map = world.getTileMap();

        GL11.glColor3d(Settings.WorldSettings.WaterColor.x, Settings.WorldSettings.WaterColor.y, Settings.WorldSettings.WaterColor.z);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(offsetX + positionX, offsetY + positionY);
        GL11.glVertex2i(offsetX + positionX + (map.getWidth() * map.getTileSize()), offsetY + positionY);
        GL11.glVertex2i(offsetX + positionX + (map.getWidth() * map.getTileSize()), offsetY + positionY + (map.getHeight() * map.getTileSize()));
        GL11.glVertex2i(offsetX + positionX, offsetY + positionY + (map.getHeight() * map.getTileSize()));
        GL11.glEnd();

        Tile[][] t = map.getTiles();

        for (int x = 0; x < t.length; x++) {
            for (int y = 0; y < t[x].length; y++) {

                if (t[x][y].getType() != TileType.Water) {
                    GL11.glColor3d(Settings.WorldSettings.MaxEnergyColor.x * (t[x][y].Energy / map.getMaxEnergyPerTile()),
                            Settings.WorldSettings.MaxEnergyColor.y * (t[x][y].Energy / map.getMaxEnergyPerTile()),
                            Settings.WorldSettings.MaxEnergyColor.z * (t[x][y].Energy / map.getMaxEnergyPerTile()));

                    TileType[] n = TileMap.GetNeighbourTypes(t, x, y);

                    if (n[0] == TileType.Water && n[1] == TileType.Water && n[2] == TileType.Land && n[3] == TileType.Land) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Land && n[1] == TileType.Water && n[2] == TileType.Water && n[3] == TileType.Land) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Land && n[1] == TileType.Land && n[2] == TileType.Water && n[3] == TileType.Water) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Water && n[1] == TileType.Land && n[2] == TileType.Land && n[3] == TileType.Water) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));

                    } else {
                        GL11.glBegin(GL11.GL_QUADS);
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + ((x + 1) * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + positionX + (x * map.getTileSize()), offsetY + positionY + ((y + 1) * map.getTileSize()));
                    }

                    GL11.glEnd();
                }
            }
        }
    }

    @Override
    public void Destroy() {

    }

    public VirtualWorld getWorld() {
        return world;
    }

    public void setWorld(VirtualWorld world) {
        this.world = world;
    }
}
