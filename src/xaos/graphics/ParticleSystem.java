package xaos.graphics;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class ParticleSystem {

    public static class Particle {
        public float x;
        public float y;
        public float z;
        public float vx;
        public float vy;
        public float vz;
        public float r;
        public float g;
        public float b;
        public float alpha;
        public float size;
        public float sizeGrow;
        public float gravity;
        public float drag;
        public int life;
        public int maxLife;

        public Particle(float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float size, float sizeGrow, float gravity, float drag, int maxLife) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = 1.0f;
            this.size = size;
            this.sizeGrow = sizeGrow;
            this.gravity = gravity;
            this.drag = drag;
            this.life = maxLife;
            this.maxLife = maxLife;
        }

        public boolean update() {
            x += vx;
            y += vy;
            z += vz;
            vz += gravity;
            vx *= drag;
            vy *= drag;
            size += sizeGrow;
            life--;
            alpha = (float) life / (float) maxLife;
            return life <= 0;
        }
    }

    private static final List<Particle> particles = new ArrayList<Particle>();
    private static final int MAX_PARTICLES = 800;

    public static void clear() {
        synchronized (particles) {
            particles.clear();
        }
    }

    public static void addParticle(float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float size, float sizeGrow, float gravity, float drag, int maxLife) {
        synchronized (particles) {
            if (particles.size() < MAX_PARTICLES) {
                particles.add(new Particle(x, y, z, vx, vy, vz, r, g, b, size, sizeGrow, gravity, drag, maxLife));
            }
        }
    }

    public static void spawnBurst(float x, float y, float z, float r, float g, float b, int count, float speed, float size) {
        for (int i = 0; i < count; i++) {
            float vx = (float) ((Math.random() - 0.5) * speed * 0.15f);
            float vy = (float) ((Math.random() - 0.5) * speed * 0.15f);
            float vz = (float) (Math.random() * speed * 0.10f);
            int life = 20 + (int) (Math.random() * 25);
            addParticle(x, y, z, vx, vy, vz, r, g, b, size, 0.0f, -0.003f, 0.96f, life);
        }
    }

    public static void spawnStoneChips(float x, float y, float z) {
        spawnBurst(x, y, z, 0.70f, 0.68f, 0.65f, 10, 1.2f, 3.0f);
        // Add a few dust puffs
        for (int i = 0; i < 3; i++) {
            float vx = (float) ((Math.random() - 0.5) * 0.04f);
            float vy = (float) ((Math.random() - 0.5) * 0.04f);
            addParticle(x, y, z, vx, vy, 0.02f, 0.85f, 0.82f, 0.78f, 3.5f, 0.1f, 0.001f, 0.92f, 25);
        }
    }

    public static void spawnWoodDebris(float x, float y, float z) {
        spawnBurst(x, y, z, 0.60f, 0.42f, 0.22f, 8, 1.0f, 3.0f);
        // Green leaves
        for (int i = 0; i < 4; i++) {
            float vx = (float) ((Math.random() - 0.5) * 0.08f);
            float vy = (float) ((Math.random() - 0.5) * 0.08f);
            float vz = (float) (0.05f + Math.random() * 0.05f);
            addParticle(x, y, z, vx, vy, vz, 0.28f, 0.65f, 0.22f, 3.0f, -0.02f, -0.002f, 0.95f, 30);
        }
    }

    public static void spawnBloodSparks(float x, float y, float z) {
        spawnBurst(x, y, z, 0.92f, 0.12f, 0.12f, 12, 1.4f, 3.5f);
    }

    public static void spawnForgeEmbers(float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            float vx = (float) ((Math.random() - 0.5) * 0.05f);
            float vy = (float) ((Math.random() - 0.5) * 0.05f);
            float vz = (float) (0.04f + Math.random() * 0.06f);
            int life = 25 + (int) (Math.random() * 20);
            addParticle(x, y, z, vx, vy, vz, 1.0f, 0.65f, 0.15f, 2.5f, -0.04f, 0.001f, 0.98f, life);
        }
    }

    public static void spawnChimneySmoke(float x, float y, float z) {
        float vx = (float) ((Math.random() - 0.5) * 0.02f + 0.01f);
        float vy = (float) ((Math.random() - 0.5) * 0.02f + 0.01f);
        float vz = (float) (0.03f + Math.random() * 0.03f);
        int life = 40 + (int) (Math.random() * 25);
        addParticle(x, y, z, vx, vy, vz, 0.75f, 0.75f, 0.78f, 4.0f, 0.15f, 0.001f, 0.97f, life);
    }

    public static void spawnHarvestLeafPuff(float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            float vx = (float) ((Math.random() - 0.5) * 0.08f);
            float vy = (float) ((Math.random() - 0.5) * 0.08f);
            float vz = (float) (0.04f + Math.random() * 0.06f);
            addParticle(x, y, z, vx, vy, vz, 0.90f, 0.80f, 0.35f, 3.0f, -0.02f, -0.002f, 0.94f, 30);
        }
    }

    public static void update() {
        synchronized (particles) {
            for (int i = particles.size() - 1; i >= 0; i--) {
                if (particles.get(i).update()) {
                    particles.remove(i);
                }
            }
        }
    }

    public static void render(int iBaseXGeneral, int iBaseYGeneral, int zView) {
        synchronized (particles) {
            if (particles.isEmpty()) return;

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            for (int i = 0; i < particles.size(); i++) {
                Particle p = particles.get(i);
                if (Math.abs(p.z - zView) <= 1.0f) {
                    int screenX = iBaseXGeneral + (int) ((p.x + p.y) * 16.0f);
                    int screenY = iBaseYGeneral - (int) ((p.x - p.y) * 8.0f);

                    if (zView != (int) p.z) {
                        screenY += (zView - (int) p.z) * 16;
                    }

                    GL11.glColor4f(p.r, p.g, p.b, Math.max(0f, Math.min(1f, p.alpha)));
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex2f(screenX - p.size, screenY - p.size);
                    GL11.glVertex2f(screenX + p.size, screenY - p.size);
                    GL11.glVertex2f(screenX + p.size, screenY + p.size);
                    GL11.glVertex2f(screenX - p.size, screenY + p.size);
                    GL11.glEnd();
                }
            }

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
