package SkyNetJR.VirtualWorld;

import SkyNetJR.Utils.Timer;

public class WorldSimulationThread extends Thread {
    private VirtualWorld world;
    private boolean destroy;
    private Object destroyedHandle;
    public final Object StopLock;

    public WorldSimulationThread(VirtualWorld w){
        StopLock = new Object();
        world = w;
        destroy = false;
        destroyedHandle = new Object();
    }

    public void Destroy(){
        destroy = true;
        try {
            destroyedHandle.wait(3000);
            if (this.getState() != State.TERMINATED)
                this.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Timer t = new Timer();

        while (true) {
            try {
                if (!world.isRunning()) {
                    StopLock.wait();
                }

                if (destroy) {
                    destroyedHandle.notifyAll();
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
