/*
* Basisklasse, die Zerstörbarkeit für Threads vererbt
* */

package SkyNetJR.Threading;

public abstract class DestroyableThread extends Thread {
    protected final Object destroyedHandle;
    protected boolean destroy;

    protected DestroyableThread() {
        this.destroyedHandle = new Object();
        destroy = false;
    }

    // Ermittelt, ob Thread beendet werden soll
    protected boolean ShouldExit(){
        if (destroy) {
            synchronized (destroyedHandle) {
                destroyedHandle.notifyAll();
            }
            return true;
        }else return false;
    }

    // Getter
    public Object getDestroyedHandle() {
        return destroyedHandle;
    }

    // Thread anhalten und zerstören
    public void Destroy() {
        destroy = true;

        // Deadlock durch Selbstzerstörung verhindern
        if (Thread.currentThread().getId() == this.getId())
            return;

        try {
            if (this.getState() != State.TERMINATED)
                synchronized (destroyedHandle) {
                    destroyedHandle.wait(3000);
                }

            if (this.getState() != State.TERMINATED)
                this.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
