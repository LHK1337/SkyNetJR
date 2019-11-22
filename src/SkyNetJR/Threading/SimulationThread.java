package SkyNetJR.Threading;

import SkyNetJR.Creatures.Population;
import SkyNetJR.Utils.Timer;
import SkyNetJR.VirtualWorld.VirtualWorld;

import java.util.Stack;
import java.util.concurrent.*;

public class SimulationThread extends DestroyableThread {
    private VirtualWorld _world;
    private Population _population;

    private long _timePrecision;
    private long _lastSimulationTime;
    private boolean _started;
    private boolean _realTime;
    private boolean _multiThread;
    private ExecutorService _threadPool;
    private int _threadPoolSize;

    public long getTimePrecision() { return _timePrecision; }
    public long getLastSimulationTime(){
        return _lastSimulationTime;
    }
    public boolean isStarted() { return _started; }
    public boolean isRealTime() { return _realTime; }
    public boolean isMultiThreaded() { return _multiThread; }

    public void setTimePrecision(long v) { _timePrecision = v; }
    public void setRealTime(boolean v) { _realTime = v; }


    public SimulationThread(VirtualWorld world, Population population, long timePrecision, boolean multiThread, boolean realTime) {
        _world = world;
        _population = population;
        _timePrecision = timePrecision;

        _realTime = realTime;

        _multiThread = multiThread;
        if (multiThread){
            _threadPoolSize = Runtime.getRuntime().availableProcessors() * 3;
            _threadPool = Executors.newFixedThreadPool(_threadPoolSize);
        }
    }
    public SimulationThread(VirtualWorld world, Population population, boolean multiThread, long timePrecision) {
        this(world, population, timePrecision, multiThread, true);
    }


    @Override
    public void run() {
        Thread.currentThread().setName("SkyNetJR.MainSimulationThread");

        Timer realTimeTimer = new Timer();

        while (!ShouldExit()){
            realTimeTimer.start();

            // update world
            if (!_multiThread || _threadPool != null)
                _world.getTileMap().Update(_timePrecision);
            else {
                Stack<Future> f = new Stack<>();

                for (int i = 0; i < _threadPoolSize; i++) {
                    int finalI = i;

                    f.push(_threadPool.submit(() -> {
                        _world.getTileMap().Update(_timePrecision, finalI, _threadPoolSize);
                    }));
                }

                while (!f.empty()) {
                    try {
                        f.pop().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            _world.setLastSimulationTime(realTimeTimer.getCurrentTime());

            if (ShouldExit()) break;

            // update creatures
            _population.Update(_timePrecision, _multiThread, _threadPool);
            _population.setLastSimulationTime(realTimeTimer.getCurrentTime());

            realTimeTimer.end();
            _lastSimulationTime = realTimeTimer.getTotalTime();
            if (_realTime && _timePrecision - _lastSimulationTime > 0) {
                try {
                    Thread.sleep(_timePrecision - _lastSimulationTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized void start() {
        _started = true;

        super.start();
    }

    @Override
    public void Destroy() {
        super.Destroy();

        if (_multiThread) {
            ExecutorService tp = _threadPool;
            _threadPool = null;

            tp.shutdown();
            try {
                if (!tp.awaitTermination(3000, TimeUnit.MILLISECONDS))
                    tp.shutdownNow();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
