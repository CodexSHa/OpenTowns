package xaos.graphics.shaders;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;
import xaos.utils.Log;

/**
 * Manages the compilation, linking, and uniform parameters of a GLSL shader program.
 */
public class ShaderProgram {

    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;
    private final Map<String, Integer> uniformLocationCache = new HashMap<String, Integer>();
    private boolean isLinked = false;

    public ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Failed to allocate OpenGL shader program ID");
        }

        vertexShaderId = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderSource);
        fragmentShaderId = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);

        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            Log.log(Log.LEVEL_ERROR, "GLSL Program Link Error:\n" + log, "ShaderProgram");
            throw new RuntimeException("GLSL Program Link Error: " + log);
        }

        GL20.glValidateProgram(programId);
        isLinked = true;
    }

    private int compileShader(int type, String source) {
        int shaderId = GL20.glCreateShader(type);
        if (shaderId == 0) {
            throw new RuntimeException("Failed to allocate OpenGL shader ID for type " + type);
        }

        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId, 1024);
            String typeName = (type == GL20.GL_VERTEX_SHADER) ? "Vertex" : "Fragment";
            Log.log(Log.LEVEL_ERROR, "GLSL " + typeName + " Shader Compile Error:\n" + log, "ShaderProgram");
            GL20.glDeleteShader(shaderId);
            throw new RuntimeException("GLSL " + typeName + " Shader Compile Error: " + log);
        }

        return shaderId;
    }

    public void bind() {
        if (isLinked) {
            GL20.glUseProgram(programId);
        }
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public int getUniformLocation(String uniformName) {
        if (uniformLocationCache.containsKey(uniformName)) {
            return uniformLocationCache.get(uniformName);
        }
        int location = GL20.glGetUniformLocation(programId, uniformName);
        uniformLocationCache.put(uniformName, location);
        return location;
    }

    public void setUniform1f(String name, float value) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform1f(loc, value);
        }
    }

    public void setUniform2f(String name, float x, float y) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform2f(loc, x, y);
        }
    }

    public void setUniform3f(String name, float x, float y, float z) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform3f(loc, x, y, z);
        }
    }

    public void setUniform4f(String name, float x, float y, float z, float w) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform4f(loc, x, y, z, w);
        }
    }

    public void setUniform1i(String name, int value) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform1i(loc, value);
        }
    }

    public void setUniform4fv(String name, float[] values) {
        int loc = getUniformLocation(name);
        if (loc != -1) {
            GL20.glUniform4fv(loc, values);
        }
    }

    public void destroy() {
        unbind();
        if (programId != 0) {
            if (vertexShaderId != 0) {
                GL20.glDetachShader(programId, vertexShaderId);
                GL20.glDeleteShader(vertexShaderId);
            }
            if (fragmentShaderId != 0) {
                GL20.glDetachShader(programId, fragmentShaderId);
                GL20.glDeleteShader(fragmentShaderId);
            }
            GL20.glDeleteProgram(programId);
        }
    }
}
