package SkyNetJR.VirtualWorld;

public class VirtualWorld {
    private TileMap tileMap;
    private long lastSimulatedFrameTime;
    private int timePrecision;
    private boolean isRunning;
    private boolean realTime;

    private WorldSimulationThread worldSimulationThread;

    public VirtualWorld(TileMap tileMap, int timePrecision) {
        this.tileMap = tileMap;
        this.timePrecision = timePrecision;

        isRunning = false;
        realTime = true;

        worldSimulationThread = new WorldSimulationThread(this);
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public int getTimePrecision() {
        return timePrecision;
    }

    public void setTimePrecision(int timePrecision) {
        this.timePrecision = timePrecision;
    }

    public long getLastSimulatedFrameTime() {
        return lastSimulatedFrameTime;
    }

    public void setLastSimulatedFrameTime(long lastSimulatedFrameTime) {
        this.lastSimulatedFrameTime = lastSimulatedFrameTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;

        if (running) {
            synchronized (worldSimulationThread.StopLock) {
                worldSimulationThread.StopLock.notify();
            }

            if (worldSimulationThread.getState() == Thread.State.NEW)
                worldSimulationThread.start();
        }
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public void Destroy() {
        worldSimulationThread.Destroy();
    }
}
