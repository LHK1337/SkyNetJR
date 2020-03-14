/*
* Helferklasse, Stoppuhr
* basiert auf der Systemzeit
* */

package SkyNetJR.Utils;

public class Stopwatch {

    private long _startTime = 0;
    private long _endTime = 0;

    // Speichert Anfangszeit - Stoppuhr starten
    public void start() {
        this._startTime = System.currentTimeMillis();
    }

    // Speichert Endzeit - Stoppuhr stoppen bzw. Runde messen
    public void end() {
        this._endTime = System.currentTimeMillis();
    }

    // Getter
    public long getStartTime() {
        return this._startTime;
    }

    public long getEndTime() {
        return this._endTime;
    }

    public long getCurrentTime() { return System.currentTimeMillis() - this._startTime; }

    public long getTotalTime() {
        return this._endTime - this._startTime;
    }
}