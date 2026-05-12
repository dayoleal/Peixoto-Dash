package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Comparator;

public class GameScreen implements Screen {

    public static final float groundY = 75f;
    private final PeixotoDash game;
    private final Viewport viewport;

    private final Texture bgTexture;
    private final Texture[] obstacleTextures;
    private final Texture[] runFrames;   
    private final Texture[] sideFrames;  

    private static final String[] obstaclePaths = {
        "sprites/obstacles/bottle.png",
        "sprites/obstacles/can.png",
        "sprites/obstacles/lantern.png",
        "sprites/obstacles/barrel.png",
        "sprites/obstacles/wheels.png",
        "sprites/obstacles/bag.png"
    };
    
    private final Player player;

    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private float spawnTimer = 0f;
    private float spawnInterval = 2.0f;

    private float speedMultiplier = 1.0f;
    private static final float maxSpeedMultiplier = 3.2f;
    private static final float speedRamp = 0.06f;

    private int lives = 3;
    private int score = 0;
    private float scoreTimer = 0f;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private final Array<Obstacle> drawList = new Array<Obstacle>();

    private static final Comparator<Obstacle> DEPTH_CMP = new Comparator<Obstacle>() {
        @Override
        public int compare(Obstacle a, Obstacle b) {
            return Float.compare(a.getDepth(), b.getDepth());
        }
    };

    public GameScreen(PeixotoDash game) {
        this.game = game;
        viewport  = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);

        bgTexture = new Texture(Gdx.files.internal("scene.png"));

         obstacleTextures = new Texture[obstaclePaths.length];
        for (int i = 0; i < obstaclePaths.length; i++)
            obstacleTextures[i] = new Texture(Gdx.files.internal(obstaclePaths[i]));

        runFrames  = new Texture[7];
        for (int i = 0; i < 7; i++)
            runFrames[i]  = new Texture(Gdx.files.internal("sprites/running/"  + (i + 1) + ".png"));

        sideFrames = new Texture[6];
        for (int i = 0; i < 6; i++)
            sideFrames[i] = new Texture(Gdx.files.internal("sprites/sideways/" + (i + 1) + ".png"));

        player = new Player(runFrames, sideFrames);

        font   = new BitmapFont();
        font.getData().setScale(2.5f);
        layout = new GlyphLayout();
    }

    @Override public void show(){}
    @Override public void hide(){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
            drawBackground();
            drawObstacles();
            player.draw(game.batch);
            drawHUD();
        game.batch.end();
    }

    private void update(float delta) {
        speedMultiplier = Math.min(speedMultiplier + speedRamp * delta, maxSpeedMultiplier);
        spawnInterval = Math.max(0.85f, 2.0f - (speedMultiplier - 1f) * 0.38f);

        player.update(delta);

        scoreTimer += delta;
        if (scoreTimer >= 0.5f) {
            score      += 10;
            scoreTimer -= 0.5f;
        }

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnObstacle();
        }

        Array<Obstacle> toRemove = new Array<Obstacle>();
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obs = obstacles.get(i);
            obs.update(delta, speedMultiplier);

            if (!obs.isActive()) {
                if (!obs.isScored()) { score += 30; obs.markScored(); }
                toRemove.add(obs);
                continue;
            }

            if (!player.isInvincible() && obs.isInHitZone()) {
                if (obs.getLane() == player.getCurrentLane()) {
                    lives--;
                    player.triggerInvincibility();
                    obs.setActive(false);
                    toRemove.add(obs);
                    if (lives <= 0) {
                        dispose();
                        return;
                    }
                }
            }
        }
        obstacles.removeAll(toRemove, true);
    }

    private void spawnObstacle() {
        int lane   = MathUtils.random(0, 2);
        int texIdx = MathUtils.random(0, obstacleTextures.length - 1);
        obstacles.add(new Obstacle(obstacleTextures[texIdx], lane));
    }

    private void drawBackground() {
        // Completely static — no scroll
        game.batch.draw(bgTexture, 0, 0, PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
    }

    private void drawObstacles() {
        // Copy, sort by depth (far first), then draw
        drawList.clear();
        drawList.addAll(obstacles);
        drawList.sort(DEPTH_CMP);
        for (int i = 0; i < drawList.size; i++) {
            drawList.get(i).draw(game.batch);
        }
    }

    private void drawHUD() {
        font.setColor(Color.WHITE);
        font.getData().setScale(2.5f);
        font.draw(game.batch, "SCORE: " + score, 20f, PeixotoDash.VIRTUAL_HEIGHT - 15f);

        StringBuilder hearts = new StringBuilder("VIDAS: ");
        for (int i = 0; i < lives; i++) hearts.append("\u2665 ");
        for (int i = lives; i < 3;  i++) hearts.append("\u2661 ");
        font.draw(game.batch, hearts.toString(), 20f, PeixotoDash.VIRTUAL_HEIGHT - 60f);

        font.setColor(Color.YELLOW);
        String vel = String.format("VEL: %.1fx", speedMultiplier);
        layout.setText(font, vel);
        font.draw(game.batch, vel,
            PeixotoDash.VIRTUAL_WIDTH - layout.width - 20f,
            PeixotoDash.VIRTUAL_HEIGHT - 15f);

        font.getData().setScale(1.3f);
        font.setColor(new Color(1f, 1f, 1f, 0.65f));
        font.draw(game.batch, "A/D ou <- -> para mover", 20f, 38f);

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        bgTexture.dispose();
        for (Texture t : obstacleTextures) if (t != null) t.dispose();
        for (Texture t : runFrames)   if (t != null) t.dispose();
        for (Texture t : sideFrames)  if (t != null) t.dispose();
        font.dispose();
    }
}

