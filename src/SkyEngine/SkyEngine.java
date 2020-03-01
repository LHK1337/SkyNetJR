package SkyEngine;

import SkyEngine.StateMachine.StateMachine;
import SkyEngine.Window.Window;
import SkyEngine.Window.WindowType;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;

public class SkyEngine implements Runnable {
    private Map<WindowType, Window> _windows;
    private StateMachine _stateMachine;
    private Thread _engineThread;

    private PrintStream _stdout;
    private PrintStream _stderr;

    public SkyEngine() { this(System.out, System.err); }
    public SkyEngine(PrintStream stdout, PrintStream stderr){
        _stderr = stderr;
        _stdout = stdout;

        _engineThread = new Thread(this, "SkyEngine::MAIN_THREAD");
    }

    private void init(){
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            _stderr.println("GLFW.glfwinit() \u2715!");
            throw new RuntimeException("GLFW.glfwinit() failed!");
        }
        _stdout.println("GLFW.glfwinit() \u2713");

        _windows = new EnumMap<WindowType, Window>(WindowType.class);
        _windows.put(WindowType.MainView,
                new Window(Settings.MainView.InitWidth,
                        Settings.MainView.InitHeight,
                        Settings.MainView.InitTitle,
                        true, true));
        _stdout.println("MainView Creation \u2713");

        _stateMachine = new StateMachine();
    }

    protected void finalize(){
        _stateMachine.Clear();

        for (Window w : _windows.values())
            w.destroy();

        GLFW.glfwTerminate();
    }

    @Override
    public void run() {
        init();
    }
}
