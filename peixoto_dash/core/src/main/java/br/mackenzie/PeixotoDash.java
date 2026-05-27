package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PeixotoDash extends Game {

    public SpriteBatch batch;

    public static final int VIRTUAL_WIDTH  = 1280;
    public static final int VIRTUAL_HEIGHT = 720;

    public static final boolean USE_ARDUINO = false;
    public static final String  ARDUINO_PORT = "COM3";

    public InputController input;

    public int highestUnlockedLevel = 1;

    public RehabMetrics[] lastMetricsPerLevel = new RehabMetrics[3];

    @Override
    public void create() {
        batch = new SpriteBatch();

        if (USE_ARDUINO) {
            input = new InputController(ARDUINO_PORT);
        } else {
            input = new InputController(ARDUINO_PORT);
        }

        setScreen(new MenuScreen(this));
    }

    public void recordLevelMetrics(RehabMetrics m) {
        lastMetricsPerLevel[m.level.number - 1] = m;
        if (m.passedLevel() && m.level.number == highestUnlockedLevel
            && highestUnlockedLevel < LevelConfig.ALL.length) {
            highestUnlockedLevel++;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
