package xaos.graphics;

public class Animation {

    public enum AnimationState {
        IDLE,
        WALK,
        MINE,
        ATTACK
    }

    private AnimationState state;
    private int currentFrame;
    private int totalFrames;
    private int frameDelay;
    private int currentDelay;
    private boolean looping;

    public Animation() {
        this(AnimationState.IDLE, 4, 10, true);
    }

    public Animation(AnimationState state, int totalFrames, int frameDelay, boolean looping) {
        this.state = state;
        this.totalFrames = totalFrames;
        this.frameDelay = frameDelay;
        this.looping = looping;
        this.currentFrame = 0;
        this.currentDelay = 0;
    }

    public void update() {
        currentDelay++;
        if (currentDelay >= frameDelay) {
            currentDelay = 0;
            currentFrame++;
            if (currentFrame >= totalFrames) {
                if (looping) {
                    currentFrame = 0;
                } else {
                    currentFrame = totalFrames - 1;
                }
            }
        }
    }

    public static float lerp(float start, float end, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return start + t * (end - start);
    }

    public AnimationState getState() {
        return state;
    }

    public void setState(AnimationState state) {
        if (this.state != state) {
            this.state = state;
            this.currentFrame = 0;
            this.currentDelay = 0;
            switch (state) {
                case WALK:
                    this.totalFrames = 6;
                    this.frameDelay = 5;
                    this.looping = true;
                    break;
                case MINE:
                    this.totalFrames = 4;
                    this.frameDelay = 7;
                    this.looping = true;
                    break;
                case ATTACK:
                    this.totalFrames = 4;
                    this.frameDelay = 4;
                    this.looping = false;
                    break;
                case IDLE:
                default:
                    this.totalFrames = 4;
                    this.frameDelay = 10;
                    this.looping = true;
                    break;
            }
        }
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getTotalFrames() {
        return totalFrames;
    }
}
