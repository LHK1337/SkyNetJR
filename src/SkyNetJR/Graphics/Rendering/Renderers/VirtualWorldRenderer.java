/*
* Klasse zum Rendern der Virtuellen Welt
* */

package SkyNetJR.Graphics.Rendering.Renderers;

import SkyNetJR.Graphics.Rendering.Renderer;
import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.Tile;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.TileType;
import SkyNetJR.VirtualWorld.VirtualWorld;
import org.lwjgl.opengl.GL11;

public class VirtualWorldRenderer extends Renderer {
    private VirtualWorld _world;

    public VirtualWorldRenderer(VirtualWorld world) {
        this._world = world;
    }

    @Override
    public void render(int offsetX, int offsetY) {
        if (!_world.getDraw()) return;

        TileMap map = _world.getTileMap();

        // Ozean zeichnen
        GL11.glColor3d(Settings.WorldSettings.WaterColor.x, Settings.WorldSettings.WaterColor.y, Settings.WorldSettings.WaterColor.z);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(offsetX, offsetY);
        GL11.glVertex2i(offsetX + (map.getWidth() * map.getTileSize()), offsetY);
        GL11.glVertex2i(offsetX + (map.getWidth() * map.getTileSize()), offsetY + (map.getHeight() * map.getTileSize()));
        GL11.glVertex2i(offsetX, offsetY + (map.getHeight() * map.getTileSize()));
        GL11.glEnd();

        // Land zeichnen
        Tile[][] t = map.getTiles();

        for (int x = 0; x < t.length; x++) {
            for (int y = 0; y < t[x].length; y++) {

                if (t[x][y].getType() != TileType.Water) {
                    GL11.glColor3d(Settings.WorldSettings.MaxEnergyColor.x * (t[x][y].Energy / Settings.SimulationSettings.MaxEnergyPerTile),
                            Settings.WorldSettings.MaxEnergyColor.y * (t[x][y].Energy / Settings.SimulationSettings.MaxEnergyPerTile),
                            Settings.WorldSettings.MaxEnergyColor.z * (t[x][y].Energy / Settings.SimulationSettings.MaxEnergyPerTile));

                    TileType[] n = TileMap.GetNeighbourTypes(t, x, y);

                    // Land zeichnen
                    if (n[0] == TileType.Water && n[1] == TileType.Water && n[2] == TileType.Land && n[3] == TileType.Land) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Land && n[1] == TileType.Water && n[2] == TileType.Water && n[3] == TileType.Land) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Land && n[1] == TileType.Land && n[2] == TileType.Water && n[3] == TileType.Water) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));

                    } else if (n[0] == TileType.Water && n[1] == TileType.Land && n[2] == TileType.Land && n[3] == TileType.Water) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));

                    } else {
                        GL11.glBegin(GL11.GL_QUADS);
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + (y * map.getTileSize()));
                        GL11.glVertex2i(offsetX + ((x + 1) * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));
                        GL11.glVertex2i(offsetX + (x * map.getTileSize()), offsetY + ((y + 1) * map.getTileSize()));
                    }

                    GL11.glEnd();
                }
            }
        }
    }

    // Getter und Setter
    public VirtualWorld getWorld() {
        return _world;
    }
    public void setWorld(VirtualWorld world) {
        this._world = world;
    }

    @Override
    public void destroy() { }
}
