package SkyEngine.StateMachine;

import java.util.Stack;

public class StateMachine {
    private Stack<BaseState> _stateStack;

    public StateMachine(){
        _stateStack = new Stack<>();
    }

    public void Push(BaseState state){
        if (!_stateStack.empty()){
            _stateStack.peek().Idle();
        }

        _stateStack.push(state);
        _stateStack.peek().Setup();
    }

    public void Pop(){
        if (!_stateStack.empty()){
            _stateStack.pop().Finalize();
        }
    }

    public void Setup(){
        if (!_stateStack.empty()) _stateStack.peek().Setup();
    }
    public void HandleEvents(double deltaTime, long windowHandle){
        if (!_stateStack.empty()) _stateStack.peek().HandleEvents(deltaTime, windowHandle);
    }
    public void Update(double deltaTime){
        if (!_stateStack.empty()) _stateStack.peek().Update(deltaTime);
    }
    public void Draw(){
        if (!_stateStack.empty()) _stateStack.peek().Draw();
    }
    public void Idle(){
        if (!_stateStack.empty()) _stateStack.peek().Idle();
    }
    public void Finalize(){
        if (!_stateStack.empty()) _stateStack.peek().Finalize();
    }

    public void Clear() {
        while (!_stateStack.empty()){
            Pop();
        }
    }
}
