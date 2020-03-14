/*
* Basisklasse, die Zerstörbarkeit für Threads vererbt
* */

package SkyNetJR.Threading;

public abstract class DestroyableThread extends Thread {
    protected final Object _destroyedHandle;
    protected boolean _destroy;

    protected DestroyableThread() {
        this._destroyedHandle = new Object();
        _destroy = false;
    }

    // Ermittelt, ob Thread beendet werden soll
    protected boolean shouldExit(){
        if (_destroy) {
            synchronized (_destroyedHandle) {
                _destroyedHandle.notifyAll();
            }
            return true;
        }else return false;
    }

    // Getter
    public Object getDestroyedHandle() {
        return _destroyedHandle;
    }

    // Thread anhalten und zerstören
    @Override public void destroy() {
        _destroy = true;

        // Deadlock durch Selbstzerstörung verhindern
        if (Thread.currentThread().getId() == this.getId())
            return;

        try {
            if (this.getState() != State.TERMINATED)
                synchronized (_destroyedHandle) {
                    _destroyedHandle.wait(3000);
                }

            if (this.getState() != State.TERMINATED)
                this.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
