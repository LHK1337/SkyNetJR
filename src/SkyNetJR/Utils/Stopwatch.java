/*
* Helferklasse, Stoppuhr
* */

package SkyNetJR.Utils;

public class Stopwatch {

    private long startTime = 0;
    private long endTime = 0;

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void end() {
        this.endTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public long getCurrentTime() { return System.currentTimeMillis() - this.startTime; }

    public long getTotalTime() {
        return this.endTime - this.startTime;
    }
}