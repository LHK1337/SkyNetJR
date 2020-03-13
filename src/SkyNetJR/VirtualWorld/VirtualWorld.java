/*
* Klasse und Logik für die virtuelle Welt
* */

package SkyNetJR.VirtualWorld;

public class VirtualWorld {
    // Legt fest, ob Welt gerendert werden soll
    private boolean _draw;

    private TileMap _tileMap;

    private long _lastSimulationTime;
    private boolean _isRunning;
    private boolean _realTime;

    public VirtualWorld(TileMap tileMap) {
        this._tileMap = tileMap;

        _draw = true;
        _isRunning = false;
        _realTime = true;
    }

    // Getter und Setter
    public long getLastSimulationTime() { return _lastSimulationTime; }
    public void setLastSimulationTime(long lastSimulatedFrameTime) { this._lastSimulationTime = lastSimulatedFrameTime; }

    public boolean isRunning() { return _isRunning; }
    public void setRunning(boolean running) { _isRunning = running; }

    public boolean isRealTime() { return _realTime; }
    public void setRealTime(boolean realTime) { this._realTime = realTime; }

    public TileMap getTileMap() { return _tileMap; }

    public boolean getDraw(){
        return _draw;
    }
    public void setDraw(boolean value){
        _draw = value;
    }

    public long getWorldTime() {
        return _tileMap.getMapTime();
    }

    public void Destroy() { /* Destroy */ }
}
