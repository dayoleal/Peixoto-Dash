package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ArduinoReaderThread extends Thread {

    public enum Mode { SERIAL, WOKWI }

    private volatile String lastData = "CENTER";
    private volatile boolean connected = false;

    private final Mode mode;
    private final String target; 

    private SerialPort arduinoPort;
    private Socket socket;

    public ArduinoReaderThread(String portName) {
        this(Mode.SERIAL, portName);
    }

    public ArduinoReaderThread(Mode mode, String target) {
        super("ArduinoReaderThread-" + mode);
        setDaemon(true);
        this.mode = mode;
        this.target = target;
    }

    public static ArduinoReaderThread forWokwi(String host, int port) {
        return new ArduinoReaderThread(Mode.WOKWI, host + ":" + port);
    }

    @Override
    public void run() {
        try {
            if (mode == Mode.SERIAL) {
                runSerial();
            } else {
                runWokwi();
            }
        } catch (Exception e) {
            System.err.println("[Arduino] Erro fatal na thread: " + e.getMessage());
        } finally {
            closeAll();
        }
    }

    private void runSerial() throws Exception {
        arduinoPort = SerialPort.getCommPort(target);
        arduinoPort.setBaudRate(9600);

        if (!arduinoPort.openPort()) {
            System.err.println("[Arduino] Não foi possível abrir a porta serial: " + target);
            return;
        }
        connected = true;
        System.out.println("[Arduino] Porta serial aberta: " + target);

        InputStream in = arduinoPort.getInputStream();
        StringBuilder currentLine = new StringBuilder();

        while (!Thread.currentThread().isInterrupted()) {
            if (in.available() > 0) {
                char c = (char) in.read();
                if (c == '\n' || c == '\r') {
                    if (currentLine.length() > 0) {
                        lastData = currentLine.toString().trim();
                        currentLine.setLength(0);
                    }
                } else {
                    currentLine.append(c);
                }
            } else {
                Thread.sleep(2);
            }
        }
    }

    private void runWokwi() throws Exception {
        String[] parts = target.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(0);
                connected = true;
                System.out.println("[Arduino/Wokwi] Conectado em " + target);

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

                String line;
                while (!Thread.currentThread().isInterrupted()
                       && (line = reader.readLine()) != null) {
                    lastData = line.trim();
                }
            } catch (Exception e) {
                connected = false;
                System.err.println("[Arduino/Wokwi] Aguardando simulador em "
                                   + target + " ... (" + e.getMessage() + ")");
                Thread.sleep(2000);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try { socket.close(); } catch (Exception ignored) {}
                }
            }
        }
    }

    private void closeAll() {
        if (arduinoPort != null && arduinoPort.isOpen()) {
            arduinoPort.closePort();
            System.out.println("[Arduino] Porta serial fechada.");
        }
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (Exception ignored) {}
            System.out.println("[Arduino/Wokwi] Socket fechado.");
        }
        connected = false;
    }

    public String getLastData()  { return lastData; }
    public boolean isConnected() { return connected; }
    public Mode getMode()        { return mode; }

    public void stopReading() {
        this.interrupt();
    }
}
