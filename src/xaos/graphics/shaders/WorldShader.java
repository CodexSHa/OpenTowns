package xaos.graphics.shaders;

import java.util.List;
import xaos.utils.LightSource;
import xaos.utils.LightingManager;

/**
 * High-performance GLSL World Shader providing dynamic torch lighting,
 * smooth additive illumination, and animated water caustics.
 */
public class WorldShader {

    private static final String VERTEX_SHADER = 
        "#version 120\n" +
        "varying vec4 v_Color;\n" +
        "varying vec2 v_TexCoord;\n" +
        "varying vec2 v_WorldPos;\n" +
        "void main() {\n" +
        "    v_Color = gl_Color;\n" +
        "    v_TexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;\n" +
        "    v_WorldPos = gl_Vertex.xy;\n" +
        "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
        "}\n";

    private static final String FRAGMENT_SHADER = 
        "#version 120\n" +
        "uniform sampler2D u_Texture;\n" +
        "uniform float u_Time;\n" +
        "uniform int u_NumLights;\n" +
        "uniform vec4 u_LightPos[16];\n" +
        "uniform vec4 u_LightColor[16];\n" +
        "uniform int u_WaterEffect;\n" +
        "varying vec4 v_Color;\n" +
        "varying vec2 v_TexCoord;\n" +
        "varying vec2 v_WorldPos;\n" +
        "void main() {\n" +
        "    vec4 texColor = texture2D(u_Texture, v_TexCoord);\n" +
        "    if (texColor.a < 0.01) {\n" +
        "        discard;\n" +
        "    }\n" +
        "    vec4 baseColor = texColor * v_Color;\n" +
        "    if (u_WaterEffect == 1) {\n" +
        "        float wave = sin(v_WorldPos.x * 0.08 + u_Time * 3.0) * cos(v_WorldPos.y * 0.08 + u_Time * 2.0);\n" +
        "        baseColor.rgb += vec3(0.08, 0.14, 0.20) * wave;\n" +
        "    }\n" +
        "    vec3 lightAdd = vec3(0.0);\n" +
        "    for (int i = 0; i < u_NumLights; i++) {\n" +
        "        vec2 lightScreenPos = u_LightPos[i].xy;\n" +
        "        float radius = u_LightPos[i].z;\n" +
        "        float intensity = u_LightPos[i].w;\n" +
        "        float dist = distance(v_WorldPos, lightScreenPos);\n" +
        "        if (dist < radius) {\n" +
        "            float atten = clamp(1.0 - (dist / radius), 0.0, 1.0);\n" +
        "            atten = atten * atten * intensity;\n" +
        "            lightAdd += u_LightColor[i].rgb * atten * 0.50;\n" +
        "        }\n" +
        "    }\n" +
        "    vec3 resultRgb = clamp(baseColor.rgb + (texColor.rgb * lightAdd), 0.0, 1.0);\n" +
        "    gl_FragColor = vec4(resultRgb, baseColor.a);\n" +
        "}\n";

    private final ShaderProgram program;
    private float totalTime = 0.0f;

    public WorldShader() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public void bind() {
        program.bind();
        program.setUniform1i("u_Texture", 0);
    }

    public void unbind() {
        program.unbind();
    }

    public void update(float deltaSeconds, int screenWidth, int screenHeight, int baseX, int baseY, int zView) {
        totalTime += deltaSeconds;
        program.setUniform1f("u_Time", totalTime);

        // Upload active point lights from LightingManager
        List<LightSource> lights = LightingManager.getLightSources();
        int maxLights = Math.min(lights.size(), 16);
        program.setUniform1i("u_NumLights", maxLights);

        for (int i = 0; i < maxLights; i++) {
            LightSource light = lights.get(i);
            // Convert grid tile (x, y, z) to screen coords
            int screenX = baseX + (light.getX() + light.getY()) * (32);
            int screenY = baseY - (light.getX() - light.getY()) * (16);
            if (zView != light.getZ()) {
                screenY += ((zView - light.getZ()) * 32);
            }

            float screenRadius = light.getRadius() * 40.0f;
            float flicker = light.getIntensity();

            program.setUniform4f("u_LightPos[" + i + "]", screenX, screenY, screenRadius, flicker);
            program.setUniform4f("u_LightColor[" + i + "]", light.getR(), light.getG(), light.getB(), 1.0f);
        }
    }

    public void setWaterEffect(boolean isWater) {
        program.setUniform1i("u_WaterEffect", isWater ? 1 : 0);
    }

    public void destroy() {
        if (program != null) {
            program.destroy();
        }
    }
}
