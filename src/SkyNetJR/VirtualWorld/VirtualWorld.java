package SkyNetJR.VirtualWorld;

public class VirtualWorld {
    public static VirtualWorld Current;

    private boolean _draw;

    public boolean getDraw(){
        return _draw;
    }
    public void setDraw(boolean value){
        _draw = value;
    }

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
        Current = this;

        this._tileMap = tileMap;

        _draw = true;
        _isRunning = false;
        _realTime = true;
    }


    public void Destroy() { /* Destroy */ }

    public long getWorldTime() {
        return _tileMap.getMapTime();
    }
}
