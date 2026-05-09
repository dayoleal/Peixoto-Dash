package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PeixotoDash extends Game {
    public SpriteBatch batch;
    public static final int VIRTUAL_WIDTH  = 1280;
    public static final int VIRTUAL_HEIGHT = 720;

    @Override
    public void create() {
        batch = new SpriteBatch();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
