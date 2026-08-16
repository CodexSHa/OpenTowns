package xaos.utils;

public class LightSource {
    private int x;
    private int y;
    private int z;
    private float radius;
    private float r;
    private float g;
    private float b;
    private float intensity;

    public LightSource() {
    }

    public LightSource(int x, int y, int z, float radius, float r, float g, float b, float intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.r = r;
        this.g = g;
        this.b = b;
        this.intensity = intensity;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
