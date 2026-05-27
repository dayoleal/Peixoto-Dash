package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {

    public enum Direction { LEFT, CENTER, RIGHT }

    private ArduinoSerial arduino; 

    private Direction currentDirection = Direction.CENTER;

    private boolean pendingLeft  = false;
    private boolean pendingRight = false;

    private float totalCenterTime = 0f;
    private float totalActiveTime = 0f;
    private int directionChanges = 0;

    public InputController(String port) {
        try {
            arduino = new ArduinoSerial(port);
        } catch (Exception e) {
            System.out.println("Arduino não conectado, usando teclado.");
            arduino = null;
        }
    }

    public void update(float delta) {

        Direction next = readDirection();

        // Contador do tempo de movimento
        if (next == Direction.CENTER) {
            totalCenterTime += delta;
        } else {
            totalActiveTime += delta;
        }

        // Contador de mudança de direção
        if (next != currentDirection) {
            directionChanges++;

            if (next == Direction.LEFT)  pendingLeft  = true;
            if (next == Direction.RIGHT) pendingRight = true;

            currentDirection = next;
        }
    }

    private Direction readDirection() {
        // Leitor de data do Arduino
        Direction dir = Direction.CENTER;

        if (arduino != null) {
            String data = arduino.read();

            if (data != null) {
                data = data.trim().toUpperCase();

                if (data.contains("L")) dir = Direction.LEFT;
                else if (data.contains("R")) dir = Direction.RIGHT;
            }
        }

        // Teclado
        boolean leftKey =
            Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.LEFT);

        boolean rightKey =
                Gdx.input.isKeyPressed(Input.Keys.D) ||
                Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (leftKey) dir = Direction.LEFT;
        else if (rightKey) dir = Direction.RIGHT;

        return dir;
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

    public float getCenterRatio() {
        float total = totalCenterTime + totalActiveTime;
        return total > 0f ? totalCenterTime / total : 0f;
    }

    public float getTotalCenterTime() { return totalCenterTime; }
    public float getTotalActiveTime() { return totalActiveTime; }
    public int   getDirectionChanges() { return directionChanges; }

    public void resetMetrics() {
        totalCenterTime = 0f;
        totalActiveTime = 0f;
        directionChanges = 0;
        pendingLeft = false;
        pendingRight = false;
        currentDirection = Direction.CENTER;
    }
}
