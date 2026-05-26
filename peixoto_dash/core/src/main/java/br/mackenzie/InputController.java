package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {

    public enum Direction { LEFT, CENTER, RIGHT }
    public enum Source { KEYBOARD, ARDUINO_SERIAL, ARDUINO_WOKWI }

    private final ArduinoReaderThread arduinoReader; 
    private final Source source;

    private Direction currentDirection = Direction.CENTER;

    private boolean pendingLeft  = false;
    private boolean pendingRight = false;

    private float totalCenterTime = 0f;
    private float totalActiveTime = 0f;
    private int directionChanges = 0;

    public InputController(Source source, ArduinoReaderThread reader) {
        this.source = source;
        this.arduinoReader = reader;
    }

    public InputController() {
        this(Source.KEYBOARD, null);
    }

    public void update(float delta) {
        Direction next = readRawDirection();

        if (next == Direction.CENTER) {
            totalCenterTime += delta;
        } else {
            totalActiveTime += delta;
        }

        if (next != currentDirection) {
            directionChanges++;

            if (next == Direction.LEFT)  pendingLeft  = true;
            if (next == Direction.RIGHT) pendingRight = true;

            currentDirection = next;
        }
    }

    public boolean consumeLeft() {
        if (pendingLeft) { pendingLeft = false; return true; }
        return false;
    }

    public boolean consumeRight() {
        if (pendingRight) { pendingRight = false; return true; }
        return false;
    }

    public Direction getRawDirection() { return currentDirection; }

    public Source getSource() { return source; }


    public float getTotalCenterTime() { return totalCenterTime; }
    public float getTotalActiveTime() { return totalActiveTime; }
    public int   getDirectionChanges() { return directionChanges; }

    public float getCenterRatio() {
        float total = totalCenterTime + totalActiveTime;
        return total > 0f ? totalCenterTime / total : 0f;
    }

    public void resetMetrics() {
        totalCenterTime = 0f;
        totalActiveTime = 0f;
        directionChanges = 0;
        pendingLeft = false;
        pendingRight = false;
        currentDirection = Direction.CENTER;
    }

    private Direction readRawDirection() {
        switch (source) {
            case ARDUINO_SERIAL:
            case ARDUINO_WOKWI:
                if (arduinoReader == null) return Direction.CENTER;
                String data = arduinoReader.getLastData();
                if (data == null) return Direction.CENTER;
            
                String d = data.trim().toUpperCase();
                if (d.contains("LEFT")  || d.contains("-1")) return Direction.LEFT;
                if (d.contains("RIGHT") || d.endsWith(": 1") || d.endsWith(":1")) return Direction.RIGHT;
                return Direction.CENTER;

            case KEYBOARD:
            default:
                
                boolean leftJust  = Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
                                 || Gdx.input.isKeyJustPressed(Input.Keys.A);
                boolean rightJust = Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                                 || Gdx.input.isKeyJustPressed(Input.Keys.D);
                
                if (leftJust)  return Direction.LEFT;
                if (rightJust) return Direction.RIGHT;
                return Direction.CENTER;
        }
    }
}
