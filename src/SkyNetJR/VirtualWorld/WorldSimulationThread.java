package SkyNetJR.VirtualWorld;

import SkyNetJR.Utils.DestroyableThread;
import SkyNetJR.Utils.Timer;

public class WorldSimulationThread extends DestroyableThread {
    public final Object StopLock;
    private VirtualWorld world;

    public WorldSimulationThread(VirtualWorld w) {
        StopLock = new Object();
        world = w;
    }

    public void run() {
        Thread.currentThread().setName("WorldSimulationThread - " + world.toString());

        Timer t = new Timer();

        while (true) {
            try {
                if (!world.isRunning()) {
                    StopLock.wait();
                }

                if (destroy) {
                    synchronized (destroyedHandle) {
                        destroyedHandle.notifyAll();
                    }
                    return;
                }

                t.start();

                world.getTileMap().Update(world.getTimePrecision());

                t.end();
                world.setLastSimulatedFrameTime(t.getTotalTime());

                if (world.isRealTime())
                    if (world.getTimePrecision() > t.getTotalTime())
                        sleep(world.getTimePrecision() - t.getTotalTime());

            } catch (InterruptedException e) {
                System.out.println("WorldSimulationThread interrupted.");
            }
        }
    }
}
