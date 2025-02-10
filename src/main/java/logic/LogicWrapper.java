package logic;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
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

    SerialPort sc;
    Thread rt;
    byte[] buffer = new byte[256];
    Queue<String> out = new LinkedList<>();

    StringBuilder command = new StringBuilder();
    Queue<String> in = new LinkedList<>();

    public LogicWrapper() {
    }

    public boolean initConnection(String portName) {
        this.sc = SerialPort.getCommPorts()[1];

        sc.openPort();
        MessageListener listener = new MessageListener();
        sc.addDataListener(listener);
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
        sc.closePort();
        return true;
    }

    private static final class MessageListener implements SerialPortMessageListener {
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public byte[] getMessageDelimiter() {
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
        }
    }
}
