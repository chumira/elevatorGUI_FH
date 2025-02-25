package logic;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import gui.GuiController;
import lombok.Getter;
import lombok.Setter;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    public boolean isConnected = false;
    GuiController gui;
    public Queue<String> in = new LinkedList<>();

    private List<Passenger> removeFromElevator = new LinkedList<>();
    private List<Passenger> removeFromFloor = new LinkedList<>();

    private Integer[] clockSpeeds = {100, 250, 500, 1000, 2500, 5000, 10000};
    StringBuilder command = new StringBuilder();

    private final CommandState commandState = new CommandState(this);

    public LogicWrapper(GuiController gui) {
        this.gui = gui;
    }

    public void tickUpdate(double elapsedTime) {
        //Eingehende Befehle verarbeiten
        if (serialPort != null && serialPort.isOpen() && this.in.size() > 0) {
            for (int i = 0; i < this.in.size(); i++) {
                String a = this.in.remove();
                System.out.println("-->parsing now: " + a);
                this.commandState.parse(a);
            }
        }

        //Ausgehende Befehle senden
        if (serialPort != null && serialPort.isOpen() && this.out.size() > 0) {
            for (int i = 0; i < this.out.size(); i++) {
                String a = this.out.remove();
                System.out.println("-->transmitting now: " + a);
                sendCommand(a);
            }
        }
        if (this.commandState.init_done) {
            for (Elevator e : this.getLogic().getGrid().getElevators()
            ) {
                e.setSpeed(elapsedTime * this.gui.ELEVATOR_SPEED);
                e.updateElevation();
                //TODO error when elevatorElevation is below lowest Floor/ above highest Floor
            }
            for (Elevator e : this.getLogic().getGrid().getElevators()
            ) {
                if (!(e.getMovementDirection() == ElevatorMovement.STAND_STILL)) {

                    for (Floor f : this.logic.getGrid().getFloors()
                    ) {
                        //TODO FINE_TUNE detection range
                        if (Math.abs(e.getElevation() - f.getHeight()) < elapsedTime * (this.gui.ELEVATOR_SPEED * 0.51)) {
                            //System.out.println("ARRIVE " + e.getId() + " " + f.getId());
                            out.add("ARRIVE " + e.getId() + " " + f.getId() + "\n");
                            e.setMovementDirection(ElevatorMovement.STAND_STILL);
                        }
                    }
                }
            }

            //Fahrgaeste steigen in einen Aufzug ein falls er in seiner Ebene steht
            for (int fID = 0; fID < this.getLogic().getGrid().getFloors().length; fID++) {
                for (Passenger p : this.getLogic().getGrid().getFloors()[fID].getPassengers()
                ) {
                    if (p.getActivity() == Activity.IS_WAITING) {
                        //TODO ggf. kapazitaet??
                        //TODO Fahrgast drueckt Knopf zum ZielFloor
                        for (int eID = 0; eID < this.getLogic().getGrid().getElevators().length; eID++) {
                            if (this.getLogic().getGrid().isElevatorinFloor(eID, fID)) {
                                if (p.getActivity() == Activity.IS_WAITING && this.getLogic().getGrid().elevators[eID].doorOpen) {
                                    removeFromFloor.add(p);
                                    this.getLogic().getGrid().getElevators()[eID].getPassengers().add(p);
                                    p.setActivity(Activity.IN_ELEVATOR);
                                }
                            }
                        }
                    }
                }
                this.getLogic().getGrid().getFloors()[fID].getPassengers().removeAll(removeFromFloor);
                removeFromFloor.clear();
            }

            //Fahrgaeste steigen aus Aufzug aus
            for (int eID = 0; eID < this.getLogic().getGrid().getElevators().length; eID++) {
                if (this.getLogic().getGrid().getElevators()[eID].getMovementDirection() == ElevatorMovement.STAND_STILL && this.getLogic().getGrid().elevators[eID].doorOpen)
                    for (Passenger p : this.getLogic().getGrid().getElevators()[eID].getPassengers()
                    ) {
                        Floor f = this.getLogic().getGrid().getFloorClosestToElevator(eID);
                        if (p.getDestination().equals(f)) {
                            if (this.getLogic().getGrid().isElevatorinFloor(eID, f.id)) {
                                p.setActivity(Activity.HAS_ARRIVED);
                                removeFromElevator.add(p);
                            }
                        }
                    }
                this.getLogic().getGrid().getElevators()[eID].getPassengers().removeAll(removeFromElevator);
                removeFromElevator.clear();
            }
        }
    }

    public boolean initConnection() {
        serialPort.openPort();
        MessageListener listener = new MessageListener(this);
        serialPort.addDataListener(listener);
        isConnected = true;
        return true;
    }


    public void sendCommand(String cmd) {
        byte[] temp = cmd.getBytes();
        //System.out.println(Arrays.toString(temp));
        serialPort.writeBytes(temp, temp.length);
    }

    public boolean closeConnection() {
        serialPort.closePort();
        isConnected = false;
        return true;
    }

    /**
     * Klasse fuer das event-based reading fuer das Event: A delimited string-based message has been received
     * siehe https://fazecast.github.io/jSerialComm/
     */
    private static final class MessageListener implements SerialPortMessageListener {
        LogicWrapper logicWrapper;

        MessageListener(LogicWrapper logicWrapper) {
            this.logicWrapper = logicWrapper;
        }

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
            logicWrapper.in.add(sb.delete(sb.length() - 1, sb.length()).toString());
        }
    }
}
