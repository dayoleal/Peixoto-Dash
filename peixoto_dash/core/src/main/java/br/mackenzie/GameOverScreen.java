package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverScreen implements Screen {
    private final PeixotoDash game;
    private Texture gameOverTexture;
    private Viewport viewport;
    private final int finalScore;

    public GameOverScreen(PeixotoDash game, int score) {
        this.game = game;
        this.finalScore = score;
        gameOverTexture = new Texture(Gdx.files.internal("gameOver.png"));
        viewport = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        // Limpa a tela
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Desenha a tela de Game Over
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(gameOverTexture, 0, 0, PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);
        game.batch.end();

        // Controle de continuação do jogo
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
         || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
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
        gameOverTexture.dispose();
    }
}
