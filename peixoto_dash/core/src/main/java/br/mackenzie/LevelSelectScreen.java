package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LevelSelectScreen implements Screen {

    private final PeixotoDash game;
    private final Viewport viewport;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final GlyphLayout layout = new GlyphLayout();
    private final ShapeRenderer shapes;

    private int cursor = 0; // 0..2

    public LevelSelectScreen(PeixotoDash game) {
        this.game = game;
        viewport = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(1.6f);
        shapes = new ShapeRenderer();
        cursor = Math.max(0, game.highestUnlockedLevel - 1);
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.10f, 0.20f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < LevelConfig.ALL.length; i++) {
            boolean unlocked = (i + 1) <= game.highestUnlockedLevel;
            boolean selected = (i == cursor);
            if (selected) {
                shapes.setColor(0.20f, 0.60f, 0.95f, 1f);
            } else if (unlocked) {
                shapes.setColor(0.10f, 0.30f, 0.55f, 1f);
            } else {
                shapes.setColor(0.15f, 0.18f, 0.25f, 1f);
            }
            float cardW = 360f, cardH = 380f;
            float gap = 40f;
            float totalW = LevelConfig.ALL.length * cardW + (LevelConfig.ALL.length - 1) * gap;
            float startX = (PeixotoDash.VIRTUAL_WIDTH - totalW) / 2f;
            float x = startX + i * (cardW + gap);
            float y = (PeixotoDash.VIRTUAL_HEIGHT - cardH) / 2f - 30f;
            shapes.rect(x, y, cardW, cardH);
        }
        shapes.end();

        // Textos
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        titleFont.setColor(Color.WHITE);
        layout.setText(titleFont, "Selecione a Fase");
        titleFont.draw(game.batch, "Selecione a Fase",
                       (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f,
                       PeixotoDash.VIRTUAL_HEIGHT - 50f);

        for (int i = 0; i < LevelConfig.ALL.length; i++) {
            boolean unlocked = (i + 1) <= game.highestUnlockedLevel;
            LevelConfig lv = LevelConfig.ALL[i];

            float cardW = 360f, cardH = 380f;
            float gap = 40f;
            float totalW = LevelConfig.ALL.length * cardW + (LevelConfig.ALL.length - 1) * gap;
            float startX = (PeixotoDash.VIRTUAL_WIDTH - totalW) / 2f;
            float x = startX + i * (cardW + gap);
            float y = (PeixotoDash.VIRTUAL_HEIGHT - cardH) / 2f - 30f;

            bodyFont.getData().setScale(2.4f);
            bodyFont.setColor(unlocked ? Color.WHITE : Color.GRAY);
            bodyFont.draw(game.batch, "FASE " + lv.number, x + 30f, y + cardH - 30f);

            bodyFont.getData().setScale(1.6f);
            bodyFont.draw(game.batch, lv.name, x + 30f, y + cardH - 90f);

            bodyFont.getData().setScale(1.2f);
            bodyFont.draw(game.batch, lv.description, x + 30f, y + cardH - 140f, cardW - 60f, -1, true);

            bodyFont.getData().setScale(1.1f);
            bodyFont.setColor(unlocked ? Color.YELLOW : Color.DARK_GRAY);
            bodyFont.draw(game.batch, "Meta: " + lv.targetScore + " pts", x + 30f, y + 130f);
            bodyFont.draw(game.batch, String.format("Vel.: %.1fx - %.1fx", lv.initialSpeedMul, lv.maxSpeedMul),
                          x + 30f, y + 95f);
            bodyFont.draw(game.batch, String.format("Postura mín.: %d%%",
                                                    (int)(lv.minCenterRatio * 100)),
                          x + 30f, y + 60f);

            if (!unlocked) {
                bodyFont.setColor(Color.ORANGE);
                bodyFont.draw(game.batch, "BLOQUEADA", x + 30f, y + 30f);
            }

            RehabMetrics m = game.lastMetricsPerLevel[i];
            if (m != null) {
                bodyFont.setColor(Color.LIME);
                bodyFont.draw(game.batch,
                    String.format("Última: %d pts | %d%%",
                                  m.score, (int)(m.getPerformanceScore() * 100)),
                    x + 30f, y + 25f);
            }
        }

        bodyFont.getData().setScale(1.3f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(game.batch,
            "<- ->  Navegar    [ENTER] Jogar    [ESC] Voltar",
            40f, 40f);

        game.batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
         || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            cursor = Math.max(0, cursor - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            cursor = Math.min(LevelConfig.ALL.length - 1, cursor + 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if ((cursor + 1) <= game.highestUnlockedLevel) {
                game.setScreen(new GameScreen(game, LevelConfig.ALL[cursor]));
                dispose();
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        titleFont.dispose();
        bodyFont.dispose();
        shapes.dispose();
    }
}
