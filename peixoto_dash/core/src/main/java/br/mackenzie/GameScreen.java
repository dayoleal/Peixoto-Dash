package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Comparator;

public class GameScreen implements Screen {

    public static final float GROUND_Y = 75f;

    private final PeixotoDash game;
    private final LevelConfig level;
    private final Viewport viewport;

    private final Texture bgTexture;

    private static final String[] OBS_PATHS = {
        "sprites/obstacles/bottle.png",
        "sprites/obstacles/can.png",
        "sprites/obstacles/lantern.png",
        "sprites/obstacles/barrel.png",
        "sprites/obstacles/wheels.png",
        "sprites/obstacles/bag.png"
    };

    private final Texture[] obstacleTex;

    private final Texture[] runFrames;
    private final Texture[] sideFrames;
    private final Player    player;

    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private final Array<Obstacle> drawList = new Array<Obstacle>();

    private float spawnTimer = 0f;
    private float spawnInterval;

    private float speedMul;
    private final float maxSpeedMul;
    private final float speedRamp;
    private final float baseSpawnInterval;
    private final float minSpawnInterval;

    private int lives = 3;
    private int score = 0;
    private float scoreTimer = 0f;
    private float levelTimer = 0f;

    private boolean paused = false;
    private boolean finishing = false;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private final RehabMetrics metrics;

    private static final Comparator<Obstacle> DEPTH_CMP = new Comparator<Obstacle>() {
        @Override
        public int compare(Obstacle a, Obstacle b) {
            return Float.compare(a.getDepth(), b.getDepth());
        }
    };

    public GameScreen(PeixotoDash game, LevelConfig level) {
        this.game = game;
        this.level = level;
        this.metrics = new RehabMetrics(level);

        viewport = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);

        bgTexture = new Texture(Gdx.files.internal(level.backgroundAsset));

        obstacleTex = new Texture[OBS_PATHS.length];
        for (int i = 0; i < OBS_PATHS.length; i++)
            obstacleTex[i] = new Texture(Gdx.files.internal(OBS_PATHS[i]));

        runFrames = new Texture[7];
        for (int i = 0; i < 7; i++)
            runFrames[i] = new Texture(Gdx.files.internal("sprites/running/" + (i + 1) + ".png"));

        sideFrames = new Texture[6];
        for (int i = 0; i < 6; i++)
            sideFrames[i] = new Texture(Gdx.files.internal("sprites/sideways/" + (i + 1) + ".png"));

        player = new Player(runFrames, sideFrames);

        font = new BitmapFont();
        font.getData().setScale(2.5f);
        layout = new GlyphLayout();

        this.speedMul          = level.initialSpeedMul;
        this.maxSpeedMul       = level.maxSpeedMul;
        this.speedRamp         = level.speedRamp;
        this.baseSpawnInterval = level.baseSpawnInterval;
        this.minSpawnInterval  = level.minSpawnInterval;
        this.spawnInterval     = baseSpawnInterval;

        game.input.resetMetrics();
    }

    @Override public void show()  {}
    @Override public void hide()  {}
    @Override public void pause() {}
    @Override public void resume(){}

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }

        if (!paused && !finishing) {
            update(delta);
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
            drawBackground();
            drawObstacles();
            player.draw(game.batch);
            drawHUD();
            if (paused) drawPauseOverlay();
        game.batch.end();
    }

    private void update(float delta) {
        game.input.update(delta);

        levelTimer += delta;

        speedMul = Math.min(speedMul + speedRamp * delta, maxSpeedMul);
        spawnInterval = Math.max(minSpawnInterval,
                                 baseSpawnInterval - (speedMul - level.initialSpeedMul) * 0.38f);

        player.update(delta, game.input);

        scoreTimer += delta;
        if (scoreTimer >= 0.5f) {
            score += 10;
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
            obs.update(delta, speedMul);

            if (!obs.isActive()) {
                if (!obs.isScored()) {
                    score += 30;
                    obs.markScored();
                    metrics.onObstacleAvoided();
                }
                toRemove.add(obs);
                continue;
            }

            if (!player.isInvincible() && obs.isInHitZone()) {
                if (obs.getLane() == player.getCurrentLane()) {
                    lives--;
                    metrics.onCollision();
                    player.triggerInvincibility();
                    obs.setActive(false);
                    toRemove.add(obs);
                    if (lives <= 0) {
                        finishLevel(false);
                        return;
                    }
                }
            }
        }
        obstacles.removeAll(toRemove, true);

        if (score >= level.targetScore) {
            finishLevel(true);
        }
    }

    private void finishLevel(boolean victory) {
        if (finishing) return;
        finishing = true;
        metrics.score = score;
        metrics.durationSec = levelTimer;
        metrics.syncFromInput(game.input);

        game.recordLevelMetrics(metrics);
        game.setScreen(new ResultsScreen(game, metrics, victory));
        dispose();
    }

    private void spawnObstacle() {
        int lane   = MathUtils.random(0, 2);
        int texIdx = MathUtils.random(0, obstacleTex.length - 1);
        obstacles.add(new Obstacle(obstacleTex[texIdx], lane));
    }

    private void drawBackground() {
        game.batch.draw(bgTexture, 0, 0, PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
    }

    private void drawObstacles() {
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
        font.draw(game.batch, "SCORE: " + score + " / " + level.targetScore,
                  20f, PeixotoDash.VIRTUAL_HEIGHT - 15f);

        StringBuilder hearts = new StringBuilder("VIDAS: ");
        for (int i = 0; i < lives; i++) hearts.append("\u2665 ");
        for (int i = lives; i < 3;  i++) hearts.append("\u2661 ");
        font.draw(game.batch, hearts.toString(), 20f, PeixotoDash.VIRTUAL_HEIGHT - 60f);

        font.getData().setScale(1.8f);
        font.setColor(Color.CYAN);
        font.draw(game.batch, "FASE " + level.number + " - " + level.name,
                  20f, PeixotoDash.VIRTUAL_HEIGHT - 105f);

        font.getData().setScale(2.5f);
        font.setColor(Color.YELLOW);
        String vel = String.format("VEL: %.1fx", speedMul);
        layout.setText(font, vel);
        font.draw(game.batch, vel,
            PeixotoDash.VIRTUAL_WIDTH - layout.width - 20f,
            PeixotoDash.VIRTUAL_HEIGHT - 15f);

        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);

        font.setColor(Color.WHITE);
        String posture = String.format("Postura: %d%%",
            (int)(game.input.getCenterRatio() * 100));
        layout.setText(font, posture);
        font.draw(game.batch, posture,
            PeixotoDash.VIRTUAL_WIDTH - layout.width - 20f,
            PeixotoDash.VIRTUAL_HEIGHT - 85f);

        font.getData().setScale(1.3f);
        font.setColor(new Color(1f, 1f, 1f, 0.65f));
        font.draw(game.batch,
            "Incline a prancha (ou A/D) para trocar de faixa   |   [P] Pausar",
            20f, 38f);

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
    }

    private void drawPauseOverlay() {
        font.getData().setScale(5f);
        font.setColor(Color.WHITE);
        layout.setText(font, "PAUSADO");
        font.draw(game.batch, "PAUSADO",
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f,
            PeixotoDash.VIRTUAL_HEIGHT / 2f + 40f);

        font.getData().setScale(1.6f);
        String hint = "[P] Continuar    [M] Voltar ao Menu";
        layout.setText(font, hint);
        font.draw(game.batch, hint,
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f,
            PeixotoDash.VIRTUAL_HEIGHT / 2f - 20f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override
    public void dispose() {
        bgTexture.dispose();
        for (Texture t : obstacleTex) if (t != null) t.dispose();
        for (Texture t : runFrames)   if (t != null) t.dispose();
        for (Texture t : sideFrames)  if (t != null) t.dispose();
        font.dispose();
    }
}
