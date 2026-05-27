package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    public  static final float PLAYER_HEIGHT = 200f;
    public  static final int LANE_COUNT = 3;

    private static final float[] LANE_X = Obstacle.LANE_X;

    private final Texture[] runFrames;
    private final Texture[] sideFrames;

    private int currentLane = 1;
    private int targetLane  = 1;

    private float runTimer = 0f;
    private float runSpeed = 0.05f;
    private int runFrame = 0;

    private float sideTimer = 0f;
    private float sideSpeed = 0.09f;
    private int sideFrame = 0;
    private boolean moving = false;
    private boolean movingLeft = false;

    private float x, width, height;
    private final float y;

    private float invincibleTimer = 0f;
    private static final float INV_DURATION = 1.5f;

    public Player(Texture[] runFrames, Texture[] sideFrames) {
        this.runFrames  = runFrames;
        this.sideFrames = sideFrames;

        float aspect = (float) runFrames[0].getWidth() / runFrames[0].getHeight();
        this.height = PLAYER_HEIGHT;
        this.width  = height * aspect;
        this.y = GameScreen.GROUND_Y;
        this.x = LANE_X[currentLane] - width * 0.5f;
    }


    // Atualiza a posição do jogador
    public void update(float delta, InputController input) {
        if (!moving) {
            if (input.consumeLeft() && currentLane > 0) {
                targetLane = currentLane - 1;
                startSideways(true);
            } else if (input.consumeRight() && currentLane < LANE_COUNT - 1) {
                targetLane = currentLane + 1;
                startSideways(false);
            }
        }

        if (moving) {
            sideTimer += delta;
            if (sideTimer >= sideSpeed) {
                sideTimer -= sideSpeed;
                sideFrame++;
                if (sideFrame >= sideFrames.length) {
                    sideFrame = 0;
                    moving = false;
                    currentLane = targetLane;
                }
            }

            float startX = LANE_X[currentLane] - width * 0.5f;
            float endX   = LANE_X[targetLane]  - width * 0.5f;
            float t      = (float)(sideFrame + 1) / sideFrames.length;
            x = startX + (endX - startX) * t;

        } else {
            x = LANE_X[currentLane] - width * 0.5f;
        }

        runTimer += delta;
        if (runTimer >= runSpeed) {
            runTimer -= runSpeed;
            runFrame = (runFrame + 1) % runFrames.length;
        }

        if (invincibleTimer > 0f) invincibleTimer -= delta;
    }

    private void startSideways(boolean left) {
        moving     = true;
        movingLeft = left;
        sideFrame  = 0;
        sideTimer  = 0f;
    }

    // Desenha o player e reage a invicibilidade (Após colisão)
    public void draw(SpriteBatch batch) {
        if (invincibleTimer > 0f && (int)(invincibleTimer * 8) % 2 == 0) return;

        if (moving) {
            Texture tex = sideFrames[Math.min(sideFrame, sideFrames.length - 1)];
            if (movingLeft) {
                batch.draw(tex, x + width, y, -width, height);
            } else {
                batch.draw(tex, x, y, width, height);
            }
        } else {
            batch.draw(runFrames[runFrame], x, y, width, height);
        }
    }

    public Rectangle getBounds() {
        float sx = width * 0.25f;
        return new Rectangle(x + sx, y + 10f, width - sx * 2f, height - 20f);
    }

    public boolean isInvincible()       { return invincibleTimer > 0f; }
    public void triggerInvincibility()  { invincibleTimer = INV_DURATION; }
    public int getCurrentLane()         { return currentLane; }
    public float getX()                 { return x; }
    public float getY()                 { return y; }
    public float getWidth()             { return width; }
    public float getHeight()            { return height; }
}
