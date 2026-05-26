package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PeixotoDash extends Game {

    public SpriteBatch batch;

    public static final int VIRTUAL_WIDTH  = 1280;
    public static final int VIRTUAL_HEIGHT = 720;

    // ===== Input =====
    //   Arduino físico: USE_ARDUINO = true, USE_WOKWI = false, ARDUINO_PORT = "COM3"
    //   Wokwi: USE_ARDUINO = true, USE_WOKWI = true
    //   Sem hardware: USE_ARDUINO = false 

    public static final boolean USE_ARDUINO = false;
    public static final boolean USE_WOKWI   = true;
    public static final String  ARDUINO_PORT = "COM3";
    public static final String  WOKWI_HOST  = "localhost";
    public static final int     WOKWI_PORT  = 8080;

    public InputController input;
    private ArduinoReaderThread arduinoReader;

    public int highestUnlockedLevel = 1;

    public RehabMetrics[] lastMetricsPerLevel = new RehabMetrics[3];

    @Override
    public void create() {
        batch = new SpriteBatch();

        if (USE_ARDUINO) {
            if (USE_WOKWI) {
                arduinoReader = ArduinoReaderThread.forWokwi(WOKWI_HOST, WOKWI_PORT);
                input = new InputController(InputController.Source.ARDUINO_WOKWI, arduinoReader);
            } else {
                arduinoReader = new ArduinoReaderThread(ARDUINO_PORT);
                input = new InputController(InputController.Source.ARDUINO_SERIAL, arduinoReader);
            }
            arduinoReader.start();
        } else {
            input = new InputController(); 
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
        if (arduinoReader != null) {
            arduinoReader.stopReading();
            try { arduinoReader.join(500); } catch (InterruptedException ignored) {}
        }
        batch.dispose();
    }
}
