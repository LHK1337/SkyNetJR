package SkyNetJR.Main;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderers.VirtualWorldRenderer;
import SkyNetJR.Graphics.Rendering.View;
import SkyNetJR.Settings;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.VirtualWorld;

public class Main {
    private static Object Lock = new Object();

    public static void main(String[] args) {
        Thread.currentThread().setName("MainThread");

        WindowManager wm = new WindowManager();
        wm.Init();

        // create World view
        View worldView = new View(Settings.ViewSettings.Width, Settings.ViewSettings.Height, "SkyNetJR", true, wm);

        TileMap map = new TileMap();
        map.SetDefaults();
        map.Generate();
        VirtualWorld world = new VirtualWorld(map, Settings.SimulationSettings.TimePrecision);
        world.setRunning(true);

        worldView.getRenderers().add(new VirtualWorldRenderer(world));

        Population population = new Population(world, Settings.SimulationSettings.TimePrecision);
        population.FillPopulation();
        population.setRunning(true);


        //! GOING FOR THE REWRITE XDDDDDDDDD
        //
        //
        //
        //
        //
        //
        //
        //
        //? :,,,,,,,,,,,,,,,,(


        //worldView.getRenderers().add(new PopulationRenderer(population));



//        ValueNoise2D noise = new ValueNoise2D(1280, 720, GenerationInfo.GetDefaults());
//        noise.Calculate();

//        worldView.getRenderers().add(new NoiseRenderer(noise));

        worldView.Start();

        while (!worldView.getDestroyed()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        wm.Destroy();
    }
}
