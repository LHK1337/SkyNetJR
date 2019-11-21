package SkyNetJR.Creatures;

import SkyNetJR.AI.NeuralNetwork;
import SkyNetJR.Settings;
import SkyNetJR.Utils.DestroyableThread;
import SkyNetJR.Utils.Timer;

public class PopulationSimulationThread extends DestroyableThread {
    public final Object StopLock;
    Population population;

    private CreatureThinkingMethod creatureThinkingMethod;
    public CreatureThinkingMethod getCreatureThinkingMethod(){
        return creatureThinkingMethod;
    }
    public void setCreatureThinkingMethod(CreatureThinkingMethod value){
        creatureThinkingMethod = value;
    }

    public PopulationSimulationThread(Population p, CreatureThinkingMethod ctm){
        StopLock = new Object();
        population = p;
        creatureThinkingMethod = ctm;
    }

    @Override
    public void run() {
        System.out.println("PopulationSimulationThread - " + population.toString());
        Timer t = new Timer();

        try {

            while (!destroy) {
                if (!population.isRunning()) {
                    StopLock.wait();
                }

                t.start();

                //Todo consider locking creatures

                for (Creature c : population.getCreatures()) {
                    c.Sense();
                }

                if (destroy) break;

                switch (creatureThinkingMethod) {
                    case CpuSingleThread:
                        ThinkOnCpuSingleThread();
                        break;
                    case CpuMultiThread:
                        ThinkOnCpuMultiThread();
                        break;
                    case Gpu:
                        ThinkOnGpu();
                        break;
                }

                if (destroy) break;

                population.ClearCollisionGrid();
                population.ClearUnstagedEnergies();

                synchronized (population.getCreatures()){
                    for (int i = 0; i < population.getCreatures().size(); i++) {
                        Creature c = population.getCreatures().get(i);
                        c.Act(population.getTimePrecision());
                        if (c.isDestroyed()) i--;
                    }
                }

                if (population.getCreatures().size() < Settings.CreatureSettings.MinPopulationSize)
                    population.FillPopulation();

                t.end();
                population.setLastSimulationTime(t.getTotalTime());

                if (population.isRealTime())
                    if (population.getTimePrecision() > t.getTotalTime())
                        sleep(population.getTimePrecision() - t.getTotalTime());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("PopulationSimulationThread interrupted");
        }

        synchronized (destroyedHandle) {
            destroyedHandle.notifyAll();
        }
    }

    //TODO thinking
    public void ThinkOnCpuSingleThread() {
        for (Creature c : population.getCreatures()) {
            NeuralNetwork brain = c.getBrain();

            brain.EvaluateCpu();
        }
    }

    public void ThinkOnCpuMultiThread() {
        // TODO
    }

    public void ThinkOnGpu() {

    }
}