package SkyEngine.Window;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window{
    private final long _id;
    public long getId() { return _id; }

    private final GLFWKeyCallback _keyCallback;

    private GLCapabilities _glCaps;
    public GLCapabilities getFlCaps(){
        return _glCaps;
    }

    private boolean _vsync;
    public boolean getVsync() {
        return _vsync;
    }
    public void setVsync(boolean value) {
        _vsync = value;
    }

    private String _title;
    public String getTitle() {
        return _title;
    }
    public void setTitle(String value) {
        _title = value;
    }
    
    private boolean _resizeable;
    public boolean getResizeable(){
        return _resizeable;
    }
    public void setResizeable(boolean value){
        glfwWindowHint(GLFW_RESIZABLE, value ? 1 : 0);
        _resizeable = value;
    }

    public Window(int width, int height, String title, boolean resizeable, boolean vsync) {
        this._title = title;

        // Creating a temporary window for getting the available OpenGL version
        glfwDefaultWindowHints();
        Hide();
        long temp = glfwCreateWindow(10, 10, "", NULL, NULL);
        glfwMakeContextCurrent(temp);
        GL.createCapabilities();
        _glCaps = GL.getCapabilities();
        glfwDestroyWindow(temp);

        // Reset and set window hints
        glfwDefaultWindowHints();
        Hide();
        if (_glCaps.OpenGL32) {
            // Hints for OpenGL 3.2 core profile
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        } else if (_glCaps.OpenGL21) {
            // Hints for legacy OpenGL 2.1
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        } else {
            throw new RuntimeException("Neither OpenGL 3.2 nor OpenGL 2.1 is "
                    + "supported, you may want to update your graphics driver.");
        }

        setResizeable(resizeable);
        setVsync(vsync);

        // Create window with specified OpenGL context
        _id = glfwCreateWindow(width, height, title, NULL, NULL);
        if (_id == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window!");
        }

        // Create OpenGL context
        glfwMakeContextCurrent(_id);
        GL.createCapabilities();

        // Set key callback
        _keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                handleKeyInput(key, scancode, action, mods);
            }
        };
        glfwSetKeyCallback(_id, _keyCallback);
    }

    public void Show(){
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
    }

    public void Hide(){
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    }

    public boolean isClosing() {
        return glfwWindowShouldClose(_id);
    }

    public void update() {
        glfwSwapBuffers(_id);
        glfwPollEvents();
    }

    public void destroy() {
        glfwDestroyWindow(_id);
        _keyCallback.free();
    }

    private void handleKeyInput(int key, int scancode, int action, int mods){
        // TODO if necessary
    }
}
