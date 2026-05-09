package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;

public class Obstacle {

    public  static final float BASE_HEIGHT = 150f;

    // Perspective scale range
    private static final float MIN_SCALE = 0.3f;
    private static final float EASE = 2.4f;   

    // Depth lifecycle
    private static final float DEPTH_DESTROY  = 1.08f;

    // Collision band depth 
    public  static final float DEPTH_HIT_LO = 0.83f;
    public  static final float DEPTH_HIT_HI = 1.02f;

    // Vanishing point in screen coordinates
    public  static final float VP_X = PeixotoDash.VIRTUAL_WIDTH  * 0.5f;
    public  static final float VP_Y = PeixotoDash.VIRTUAL_HEIGHT * 0.85f;

    // Lane X at the player plane 
    public  static final float[] LANE_X = {
        PeixotoDash.VIRTUAL_WIDTH * 0.20f,
        PeixotoDash.VIRTUAL_WIDTH * 0.53f,
        PeixotoDash.VIRTUAL_WIDTH * 0.8f
    };

    // Depth speed per second 
    private static final float DEPTH_RATE = 0.2f;

    private final Texture texture;
    private final float aspect;
    private final int lane;
    private float depth = 0f;
    private boolean active = true;
    private boolean scored = false;

    public Obstacle(Texture texture, int lane) {
        this.texture = texture;
        this.lane = lane;
        this.aspect = (float) texture.getWidth() / texture.getHeight();
    }

    public void update(float delta, float speedMultiplier) {
        depth += DEPTH_RATE * speedMultiplier * delta;
        if (depth >= DEPTH_DESTROY) active = false;
    }

    private float progress() {
        float t = Math.min(Math.max(depth, 0f), 1f);
        return (float) Math.pow(t, EASE);
    }

    private float scale() {
        return MIN_SCALE + (1f - MIN_SCALE) * progress();
    }

    private float screenCX() {
        float t = Math.min(Math.max(depth, 0f), 1f);
        return VP_X + (LANE_X[lane] - VP_X) * t;
    }

    public boolean isActive() { 
        return active; 
    }
    public void setActive(boolean v) { 
        active = v; 
    }
    public int getLane() { 
        return lane; 
    }
    public float getDepth() { 
        return depth; 
    }
    public boolean isInHitZone() { 
        return depth >= DEPTH_HIT_LO && depth <= DEPTH_HIT_HI; 
    }
    public boolean isScored() { 
        return scored; 
    }
    public void markScored() { 
        scored = true; 
    }
}
