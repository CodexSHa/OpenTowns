package xaos.graphics.shaders;

import xaos.utils.Log;

/**
 * Manages the global shader pipeline and provides seamless fallback when shaders are disabled.
 */
public class ShaderManager {

    private static WorldShader worldShader = null;
    private static boolean shadersAvailable = false;
    private static boolean shadersEnabled = true;
    private static long lastFrameTime = System.currentTimeMillis();

    public static void init() {
        try {
            if (worldShader != null) {
                worldShader.destroy();
            }
            worldShader = new WorldShader();
            shadersAvailable = true;
            Log.log(Log.LEVEL_DEBUG, "GLSL Shader Pipeline initialized successfully", "ShaderManager");
        } catch (Throwable t) {
            shadersAvailable = false;
            worldShader = null;
            Log.log(Log.LEVEL_DEBUG, "GLSL Shaders unavailable, falling back to legacy pipeline: " + t.getMessage(), "ShaderManager");
        }
    }

    public static boolean isShadersActive() {
        return shadersAvailable && shadersEnabled && (worldShader != null);
    }

    public static void setShadersEnabled(boolean enabled) {
        shadersEnabled = enabled;
    }

    public static void bindWorldShader(int screenWidth, int screenHeight, int baseX, int baseY, int zView) {
        if (isShadersActive()) {
            long now = System.currentTimeMillis();
            float deltaSeconds = Math.min(0.1f, (now - lastFrameTime) / 1000.0f);
            lastFrameTime = now;

            worldShader.bind();
            worldShader.update(deltaSeconds, screenWidth, screenHeight, baseX, baseY, zView);
        }
    }

    public static void setWaterEffect(boolean isWater) {
        if (isShadersActive()) {
            worldShader.setWaterEffect(isWater);
        }
    }

    public static void unbind() {
        if (isShadersActive()) {
            worldShader.unbind();
        }
    }

    public static void destroy() {
        if (worldShader != null) {
            worldShader.destroy();
            worldShader = null;
            shadersAvailable = false;
        }
    }
}
