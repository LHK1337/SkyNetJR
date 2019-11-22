package SkyNetJR.VirtualWorld;

public class VirtualWorld {
    private TileMap _tileMap;

    private long _lastSimulationTime;
    private boolean _isRunning;
    private boolean _realTime;


    public long getLastSimulationTime() { return _lastSimulationTime; }
    public boolean isRunning() { return _isRunning; }
    public boolean isRealTime() { return _realTime; }
    public TileMap getTileMap() { return _tileMap; }

    public void setLastSimulationTime(long lastSimulatedFrameTime) { this._lastSimulationTime = lastSimulatedFrameTime; }
    public void setRunning(boolean running) { _isRunning = running; }
    public void setRealTime(boolean realTime) { this._realTime = realTime; }


    public VirtualWorld(TileMap tileMap) {
        this._tileMap = tileMap;

        _isRunning = false;
        _realTime = true;
    }


    public void Destroy() { /* Destroy */ }
}
