package SkyEngine.StateMachine;

public abstract class BaseState {

    public abstract void Setup();
    public abstract void HandleEvents(double deltaTime, long windowHandle);
    public abstract void Update(double deltaTime);
    public abstract void Draw();
    public abstract void Idle();
    public abstract void Finalize();
}
