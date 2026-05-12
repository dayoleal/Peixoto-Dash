package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Obstacle {

    public  static final float height = 150f;

    // Perspective scale range
    private static final float scale = 0.3f;
    private static final float ease = 2.4f;   

    // Depth lifecycle
    private static final float depth_lifecycle = 1.08f;

    // Collision band depth 
    public  static final float collisionBand_low = 0.83f;
    public  static final float collisionBand_higth = 1.02f;

    // Vanishing point in screen coordinates
    public  static final float vanishing_pointX = PeixotoDash.VIRTUAL_WIDTH  * 0.5f;
    public  static final float vanishing_pointY = PeixotoDash.VIRTUAL_HEIGHT * 0.85f;

    // Lane X at the player plane 
    public  static final float[] laneX = {
        PeixotoDash.VIRTUAL_WIDTH * 0.20f,
        PeixotoDash.VIRTUAL_WIDTH * 0.53f,
        PeixotoDash.VIRTUAL_WIDTH * 0.8f
    };

    // Depth speed per second 
    private static final float speed = 0.2f;

    private final Texture obstacleTexture;
    private final float obstacleAspect;
    private final int lane;
    private float depth = 0f;
    private boolean active = true;
    private boolean scored = false;

    public Obstacle(Texture obstacleTexture, int lane) {
        this.obstacleTexture = obstacleTexture;
        this.lane = lane;
        this.obstacleAspect = (float) obstacleTexture.getWidth() / obstacleTexture.getHeight();
    }

    public void update(float delta, float speedMultiplier) {
        depth += speed * speedMultiplier * delta;
        if (depth >= depth_lifecycle) active = false;
    }

    private float progress() {
        float t = Math.min(Math.max(depth, 0f), 1f);
        return (float) Math.pow(t, ease);
    }

    private float scale() {
        return scale + (1f - scale) * progress();
    }

    private float screenCX() {
        float t = Math.min(Math.max(depth, 0f), 1f);
        return vanishing_pointX + (laneX[lane] - vanishing_pointX) * t;
    }

    private float screenBottomY() {
        float t = Math.min(Math.max(depth, 0f), 1f);
        return vanishing_pointY + (GameScreen.groundY - vanishing_pointY) * t;
    }

    public void draw(SpriteBatch batch) {
        if (!active) return;
        float s  = scale();
        float h  = height * s;
        float w  = h * obstacleAspect;
        float cx = screenCX();
        float by = screenBottomY();
        batch.draw(obstacleTexture, cx - w * 0.5f, by, w, h);
    }

    public Rectangle getBounds() {
        float s  = scale();
        float h  = height * s;
        float w  = h * obstacleAspect;
        float cx = screenCX();
        float by = screenBottomY();
        float sx = w * 0.20f;
        return new Rectangle(cx - w * 0.5f + sx, by + 8f, w - sx * 2f, h * 0.80f);
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
        return depth >= collisionBand_low && depth <= collisionBand_higth; 
    }
    public boolean isScored() { 
        return scored; 
    }
    public void markScored() { 
        scored = true; 
    }
}
