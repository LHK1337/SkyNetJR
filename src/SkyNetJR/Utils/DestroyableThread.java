package SkyNetJR.Utils;

public abstract class DestroyableThread extends Thread {
    protected final Object destroyedHandle;
    protected boolean destroy;

    protected DestroyableThread() {
        this.destroyedHandle = new Object();
        destroy = false;
    }

    public void Destroy() {
        destroy = true;

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

    public Object getDestroyedHandle() {
        return destroyedHandle;
    }
}
