package SkyNetJR.Main;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderers.BrainRenderer;
import SkyNetJR.Graphics.Rendering.Renderers.PopulationRenderer;
import SkyNetJR.Graphics.Rendering.Renderers.VirtualWorldRenderer;
import SkyNetJR.Graphics.Rendering.View;
import SkyNetJR.Settings;
import SkyNetJR.Threading.SimulationThread;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.io.File;
import java.io.IOException;

public class Main {
    private static Object Lock = new Object();

    public static VirtualWorld World;
    public static Population Population;
    public static View WorldView;
    public static SimulationThread SimulationThread;

    public static void main(String[] args) {
        Thread.currentThread().setName("MainThread");

        WindowManager wm = new WindowManager();
        wm.Init();

        // create World view
        WorldView = new View(Settings.ViewSettings.Width, Settings.ViewSettings.Height, "SkyNetJR", true, wm);

        TileMap map = null;

        if (!new File("map.snet.map").exists()){
            map = new TileMap();
            map.SetDefaults();
            map.Generate();
            try {
                map.saveToFile("map.snet.map");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                map = TileMap.LoadFromFile("map.snet.map");
            } catch (IOException e) {
                new File("map.snet.map").delete();

                e.printStackTrace();
            }
        }


        World = new VirtualWorld(map);
        World.setRunning(true);
        World.setDraw(true);

        WorldView.getRenderers().add(new VirtualWorldRenderer(World));

        if (!new File("population.snet.pop").exists()) {
            Population = new Population(World);
            Population.FillPopulation();
        }
        else
            {
                try {
                    Population = SkyNetJR.Creatures.Population.LoadFromFile("population.snet.pop", World);
                } catch (IOException e) {
                    new File("population.snet.pop").delete();
                    e.printStackTrace();
                }
        }

        Population.setRunning(true);

        WorldView.getRenderers().add(new PopulationRenderer(Population));

        SimulationThread = new SimulationThread(World, Population, true, Settings.SimulationSettings.TimePrecision);
        SimulationThread.start();

//        ValueNoise2D noise = new ValueNoise2D(1280, 720, GenerationInfo.GetDefaults());
//        noise.Calculate();

//        WorldView.getRenderers().add(new NoiseRenderer(noise));

        WorldView.Start();


        View BrainView = new View(1280, 720, "BrainView", true, wm);
        BrainRenderer brainRenderer = new BrainRenderer(Population.getCreatures().get(0).getBrain());
        BrainView.getRenderers().add(brainRenderer);
        BrainView.Start();


        while (!WorldView.getDestroyed()) {
            System.out.println(
                    (SimulationThread.isRealTime() ? "[RT]" : "[FF]") +
                    " PST: " + Population.getLastSimulationTime() + "ms" +
                    " WST: " + World.getLastSimulationTime() + "ms" +
                    " CST: " + SimulationThread.getLastSimulationTime() + "ms" +
                    " | Creatures: " + Population.getCreatures().size() +
                    " | Time/Creature: " + (float)Population.getLastSimulationTime() / (float)Population.getCreatures().size() + "ms"
            );

            brainRenderer.set_nn(Population.getCreatures().get(0).getBrain());

            try {
                if (true) World.getTileMap().saveToFile("map.snet.map");
                if (true) Population.saveToFile("population.snet.pop");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BrainView.Destroy();

        SimulationThread.Destroy();

        World.Destroy();
        Population.Destroy();

        wm.Destroy();
    }
}
