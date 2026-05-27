package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoSerial {

    private SerialPort port;

    public ArduinoSerial(String portName) {
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(115200);
        port.openPort();
    }

    public String read() {
        byte[] buffer = new byte[1024];
        int numRead = port.readBytes(buffer, buffer.length);

        if (numRead > 0) {
            return new String(buffer, 0, numRead).trim();
        }
        return null;
    }
}