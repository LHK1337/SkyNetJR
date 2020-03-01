package SkyEngine.Shader;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int _programId;

    private int _vertexShaderId;

    private int _fragmentShaderId;

    public Shader() throws Exception {
        _programId = glCreateProgram();
        if (_programId == 0) {
            throw new Exception("Could not create Shader");
        }
    }

    public void createVertexShader(String shaderCode) throws Exception {
        _vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        _fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(_programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(_programId);
        if (glGetProgrami(_programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(_programId, 1024));
        }

        if (_vertexShaderId != 0) {
            glDetachShader(_programId, _vertexShaderId);
        }
        if (_fragmentShaderId != 0) {
            glDetachShader(_programId, _fragmentShaderId);
        }

        glValidateProgram(_programId);
        if (glGetProgrami(_programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(_programId, 1024));
        }

    }

    public void bind() {
        glUseProgram(_programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (_programId != 0) {
            glDeleteProgram(_programId);
        }
    }
}