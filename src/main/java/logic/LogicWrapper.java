package logic;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import gui.GuiController;
import lombok.Getter;
import lombok.Setter;


import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Setter
@Getter
public class LogicWrapper {

    Logic logic;

    SerialConnection sCon;

    SerialPort serialPort;
    Thread rt;
    byte[] buffer = new byte[256];
    Queue<String> out = new LinkedList<>();

    GuiController gui;
    public static Queue<String> in = new LinkedList<>();
    StringBuilder command = new StringBuilder();

    private final Parser parser = new Parser(this);

    public LogicWrapper(GuiController gui) {
        this.gui = gui;
    }

    public boolean initConnection() {
        serialPort.openPort();
        MessageListener listener = new MessageListener();
        serialPort.addDataListener(listener);
        //try {
        //  Thread.sleep(5000);
        //} catch (Exception e) {
        //    e.printStackTrace();
        // }
        //sc.removeDataListener();
        //sc.closePort();
        return true;
    }


    public boolean closeConnection() {
        serialPort.closePort();
        return true;
    }

    /**
     * Klasse fuer das event-based reading fuer das Event: A delimited string-based message has been received
     * siehe https://fazecast.github.io/jSerialComm/
     */
    private static final class MessageListener implements SerialPortMessageListener {
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public byte[] getMessageDelimiter() {
            //newLine Symbol als Delimiter
            return new byte[]{(byte) '\n'};
        }

        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] delimitedMessage = event.getReceivedData();
            StringBuilder sb = new StringBuilder();
            for (byte b : delimitedMessage
            ) {
                sb.append((char) b);
            }
            System.out.println("Received the following delimited message: " + sb);
            //eingegangene Nachricht speichern (ohne '\n')
            in.add(sb.delete(sb.length() - 1, sb.length()).toString());
        }
    }
}
