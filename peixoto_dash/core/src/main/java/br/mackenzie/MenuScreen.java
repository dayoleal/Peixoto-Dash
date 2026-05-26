package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen implements Screen {

    private final PeixotoDash game;
    private final Texture menuTexture;
    private final Viewport viewport;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public MenuScreen(PeixotoDash game) {
        this.game = game;
        menuTexture = new Texture(Gdx.files.internal("startMenu.png"));
        viewport = new FitViewport(PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);

        font = new BitmapFont();
        font.getData().setScale(1.5f);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        game.batch.draw(menuTexture, 0, 0,
                        PeixotoDash.VIRTUAL_WIDTH, PeixotoDash.VIRTUAL_HEIGHT);

        String inputLabel = "Controle: " + describeInput();
        Color statusColor = (game.input.getSource() == InputController.Source.KEYBOARD)
                          ? Color.YELLOW
                          : Color.LIME;
        font.setColor(statusColor);
        font.draw(game.batch, inputLabel, 20f, 40f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(game.batch, "[ESPAÇO] Iniciar    [L] Selecionar fase    [ESC] Sair",
                  20f, PeixotoDash.VIRTUAL_HEIGHT - 20f);
        font.getData().setScale(1.5f);

        game.batch.end();

        handleInput();
    }

    private String describeInput() {
        switch (game.input.getSource()) {
            case ARDUINO_SERIAL: return "Arduino (serial)";
            case ARDUINO_WOKWI:  return "Arduino via Wokwi (TCP)";
            case KEYBOARD:
            default:             return "Teclado (modo dev)";
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
         || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game, LevelConfig.LEVEL_1));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new LevelSelectScreen(game));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        menuTexture.dispose();
        font.dispose();
    }
}
