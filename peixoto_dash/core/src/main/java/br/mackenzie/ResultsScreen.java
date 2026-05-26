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

public class ResultsScreen implements Screen {

    private final PeixotoDash game;
    private final RehabMetrics m;
    private final boolean victory;
    private final Viewport viewport;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final GlyphLayout layout = new GlyphLayout();
    private final ShapeRenderer shapes;

    public ResultsScreen(PeixotoDash game, RehabMetrics metrics, boolean victory) {
        this.game = game;
        this.m = metrics;
        this.victory = victory;
        viewport = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(1.6f);
        shapes = new ShapeRenderer();
    }

    @Override public void show()  {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.08f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float barX = 200f, barY = 230f, barW = PeixotoDash.VIRTUAL_WIDTH - 400f, barH = 38f;
        shapes.setColor(0.12f, 0.16f, 0.22f, 1f);
        shapes.rect(barX, barY, barW, barH);
        shapes.setColor(0.20f, 0.85f, 0.45f, 1f);
        shapes.rect(barX, barY, barW * m.getPerformanceScore(), barH);

        shapes.end();

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        titleFont.setColor(victory ? Color.LIME : Color.SCARLET);
        String title = victory
            ? "FASE " + m.level.number + " CONCLUÍDA!"
            : "FIM DE JOGO";
        layout.setText(titleFont, title);
        titleFont.draw(game.batch, title,
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f,
            PeixotoDash.VIRTUAL_HEIGHT - 60f);

        bodyFont.getData().setScale(1.8f);
        bodyFont.setColor(Color.WHITE);
        layout.setText(bodyFont, m.level.name);
        bodyFont.draw(game.batch, m.level.name,
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f,
            PeixotoDash.VIRTUAL_HEIGHT - 130f);

        bodyFont.getData().setScale(1.6f);
        float x = 200f;
        float y = PeixotoDash.VIRTUAL_HEIGHT - 200f;
        float dy = 38f;

        drawMetric(x, y - 0 * dy, "Pontuação",
            String.format("%d / %d", m.score, m.level.targetScore),
            m.score >= m.level.targetScore);

        drawMetric(x, y - 1 * dy, "Tempo de jogo",
            String.format("%.1f s", m.durationSec), true);

        drawMetric(x, y - 2 * dy, "Controle postural (zona neutra)",
            String.format("%d%% (mín. %d%%)",
                (int)(m.getCenterRatio() * 100),
                (int)(m.level.minCenterRatio * 100)),
            m.getCenterRatio() >= m.level.minCenterRatio);

        int totalEnc = m.obstaclesAvoided + m.collisionsTaken;
        drawMetric(x, y - 3 * dy, "Colisões evitadas",
            String.format("%d / %d", m.obstaclesAvoided, totalEnc),
            m.collisionsTaken <= 1);

        drawMetric(x, y - 4 * dy, "Mudanças de direção (coordenação)",
            String.format("%d", m.directionChanges), true);

        drawMetric(x, y - 5 * dy, "Reação média",
            m.reactionSamples == 0
                ? "—"
                : String.format("%.2f s", m.averageReactionSec),
            true);

        bodyFont.getData().setScale(2.2f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(game.batch, "DESEMPENHO GERAL", 200f, 305f);

        bodyFont.getData().setScale(1.8f);
        bodyFont.setColor(Color.WHITE);
        String pct = (int)(m.getPerformanceScore() * 100) + " / 100";
        layout.setText(bodyFont, pct);
        bodyFont.draw(game.batch, pct,
            PeixotoDash.VIRTUAL_WIDTH - 200f - layout.width, 305f);

        bodyFont.getData().setScale(1.4f);
        bodyFont.setColor(Color.YELLOW);
        String msg;
        if (m.passedLevel()) {
            msg = (m.level.number < LevelConfig.ALL.length)
                ? "Próxima fase liberada!"
                : "Você completou todas as fases — parabéns!";
        } else {
            if (m.score < m.level.targetScore) {
                msg = "Tente novamente — falta pontuação para liberar a próxima fase.";
            } else {
                msg = "Tente novamente — melhore o controle postural (mantenha a prancha centrada).";
            }
        }
        layout.setText(bodyFont, msg);
        bodyFont.draw(game.batch, msg,
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f, 160f);

        bodyFont.getData().setScale(1.4f);
        bodyFont.setColor(Color.WHITE);
        String hint = "[ENTER] Jogar de novo    [L] Selecionar fase    [M] Menu";
        layout.setText(bodyFont, hint);
        bodyFont.draw(game.batch, hint,
            (PeixotoDash.VIRTUAL_WIDTH - layout.width) / 2f, 60f);

        game.batch.end();

        handleInput();
    }

    private void drawMetric(float x, float y, String label, String value, boolean good) {
        bodyFont.setColor(Color.LIGHT_GRAY);
        bodyFont.draw(game.batch, label, x, y);
        bodyFont.setColor(good ? Color.LIME : Color.SALMON);
        layout.setText(bodyFont, value);
        bodyFont.draw(game.batch, value,
            PeixotoDash.VIRTUAL_WIDTH - 200f - layout.width, y);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
         || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game, m.level));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new LevelSelectScreen(game));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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
