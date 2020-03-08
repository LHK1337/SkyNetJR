package Test;

import SkyNetJR.Creatures.Population;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        WindowManager wm = new WindowManager();
//        wm.Init();
//
//        View v = new View(1500, 920, "SkyNetJR::Test", true, wm);
//
//        v.Start();
//
//        while (!v.getDestroyed()) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        wm.Destroy();
        TileMap map = null;
        try {
            map = TileMap.LoadFromFile("map.snet.map");
        } catch (IOException e) {
            e.printStackTrace();
        }
        VirtualWorld world = new VirtualWorld(map);
        Population pop = new Population(world);
        pop.FillPopulation();

        try {
            pop.saveToFile("population.snet.pop");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Population pop2 = null;
        try {
            pop2 = Population.LoadFromFile("population.snet.pop", world);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
