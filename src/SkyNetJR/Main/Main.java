package SkyNetJR.Main;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderers.PopulationRenderer;
import SkyNetJR.Graphics.Rendering.Renderers.VirtualWorldRenderer;
import SkyNetJR.Graphics.Rendering.View;
import SkyNetJR.Settings;
import SkyNetJR.Threading.SimulationThread;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.VirtualWorld;

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

        TileMap map = new TileMap();
        map.SetDefaults();
        map.Generate();
        World = new VirtualWorld(map);
        World.setRunning(true);

        WorldView.getRenderers().add(new VirtualWorldRenderer(World));

        Population = new Population(World);
        Population.FillPopulation();
        Population.setRunning(true);

        WorldView.getRenderers().add(new PopulationRenderer(Population));

        SimulationThread = new SimulationThread(World, Population, true, Settings.SimulationSettings.TimePrecision);
        SimulationThread.start();

//        ValueNoise2D noise = new ValueNoise2D(1280, 720, GenerationInfo.GetDefaults());
//        noise.Calculate();

//        WorldView.getRenderers().add(new NoiseRenderer(noise));

        WorldView.Start();

        while (!WorldView.getDestroyed()) {
            System.out.println(
                    (SimulationThread.isRealTime() ? "[RT]" : "[FF]") +
                    " PST: " + Population.getLastSimulationTime() + "ms" +
                    " WST: " + World.getLastSimulationTime() + "ms" +
                    " CST: " + SimulationThread.getLastSimulationTime() + "ms" +
                    " | Creatures: " + Population.getCreatures().size() +
                    " | Time/Creature: " + (float)Population.getLastSimulationTime() / (float)Population.getCreatures().size() + "ms"
            );

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SimulationThread.Destroy();

        World.Destroy();
        Population.Destroy();

        wm.Destroy();
    }
}
