package logic;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import gui.GuiController;
import logic.types.Activity;
import logic.types.ElevatorMovement;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Setter
@Getter
public class Logic {

    public ElevatorGrid grid;
    SerialPort serialPort;
    private static final Logger logger = LogManager.getLogger("commands");
    private byte[] buffer = new byte[256];
    Queue<String> out = new LinkedList<>();

    public boolean isConnected = false;
    GuiController gui;
    Queue<String> in = new LinkedList<>();

    private List<Passenger> removeFromElevator = new LinkedList<>();
    private List<Passenger> removeFromFloor = new LinkedList<>();

    private Integer[] clockSpeeds = {100, 250, 500, 1000, 2500, 5000, 10000};
    StringBuilder command = new StringBuilder();

    SimTime sTime;
    private final CommandState commandState = new CommandState(this);

    public Logic(GuiController gui) {
        this.gui = gui;
    }

    /**
     * die Hauptlogik, welche in einer Schleife aufgerufen wird fuer Aufzugsbewegung, Befehle und Fahrgaeste
     *
     * @param elapsedTime die vergangene Zeit seit dem letzten Aufruf
     */
    public void tickUpdate(double elapsedTime) {
        //Eingehende Befehle verarbeiten
        if (serialPort != null && serialPort.isOpen() && this.in.size() > 0) {
            for (int i = 0; i < this.in.size(); i++) {
                String a = this.in.remove();
                logger.info("--> " + a + '\n');
                this.commandState.parse(a);
            }
        }

        //Ausgehende Befehle senden
        if (serialPort != null && serialPort.isOpen() && this.out.size() > 0) {
            for (int i = 0; i < this.out.size(); i++) {
                String a = this.out.remove();
                logger.info("<-- " + a);
                sendCommand(a);
            }
        }
        if (this.commandState.init_done) {
            for (Elevator e : this.grid.getElevators()
            ) {
                //Fehlerfall das der Aufzug unter der untersten Ebene ist
                if ((e.getElevation() + grid.getBufferHeight()) < grid.floors[0].getHeight() && !e.encounteredError) {
                    e.encounteredError = true;
                    this.grid.elevators[e.id].setMovementDirection(ElevatorMovement.STAND_STILL);
                    this.grid.bufferHeight += 0.5;
                    this.gui.displayError(
                            "elevator " + e.getId() + " reached the lowest point but has not stopped."
                            , e.getId());
                    logWarn("elevator " + e.getId() + " reached the lowest point but has not stopped.");
                } else
                    //Fehlerfall das der Aufzug ueber der obersten Ebene ist
                    if ((e.getElevation() - grid.getBufferHeight()) > grid.floors[grid.floors.length - 1].getHeight() && !e.encounteredError) {
                        e.encounteredError = true;
                        this.grid.elevators[e.id].setMovementDirection(ElevatorMovement.STAND_STILL);
                        this.grid.bufferHeight += 0.5;
                        this.gui.displayError(
                                "elevator " + e.getId() + " reached the highest point but has not stopped."
                                , e.getId());
                        logWarn("elevator " + e.getId() + " reached the highest point but has not stopped.");
                    }
                if (!e.encounteredError) {
                    //hochfahren/herunterfahren/stillstehen
                    e.updateElevation(elapsedTime * this.grid.ELEVATOR_SPEED);
                }
            }
            //check ob der Aufzug in einem Stockwerk angekommen ist
            for (Elevator e : this.getGrid().getElevators()
            ) {
                if (!(e.getMovementDirection() == ElevatorMovement.STAND_STILL)) {

                    for (Floor f : this.grid.getFloors()
                    ) {
                        if (!f.equals(e.mostRecentFloor)
                                && this.grid.isElevatorRoughlyInFloor(e.getId(), f.getId())) {
                            out.add("ARRIVE " + e.getId() + " " + f.getId() + "\n");
                            e.mostRecentFloor = f;
                            e.inBetweenFloors = false;
                        } else if (CommandState.ENABLE_LEAVE_COMMAND && f.equals(e.mostRecentFloor)
                                && this.grid.isElevatorNotInFloor(e.getId(), f.getId())
                                && !e.inBetweenFloors) {
                            out.add("LEAVE " + e.getId() + " " + f.getId() + "\n");
                            e.inBetweenFloors = true;
                        }
                    }
                }
            }

            //Fahrgaeste steigen in einen Aufzug ein falls er in seiner Ebene steht
            for (int fID = 0; fID < this.grid.getFloors().length; fID++) {
                for (Passenger p : this.grid.getFloors()[fID].getPassengers()
                ) {
                    if (p.getActivity() == Activity.IS_WAITING) {
                        //TODO ggf. kapazitaet??
                        for (int eID = 0; eID < this.grid.getElevators().length; eID++) {
                            if (this.grid.isElevatorinFloor(eID, fID)) {
                                if (p.getActivity() == Activity.IS_WAITING && this.grid.elevators[eID].doorOpen) {
                                    removeFromFloor.add(p);
                                    this.grid.getElevators()[eID].getPassengers().add(p);
                                    p.setActivity(Activity.IN_ELEVATOR);
                                    //Knopf fuer Zieletage im Aufzug druecken
                                    this.out.add("REQUEST " + eID + " " + p.destination.id + '\n');
                                    this.gui.showDestination(true, eID, p.destination.id);
                                }
                            }
                        }
                    }
                }
                this.grid.getFloors()[fID].getPassengers().removeAll(removeFromFloor);
                removeFromFloor.clear();
            }

            //Fahrgaeste steigen aus Aufzug aus
            for (int eID = 0; eID < this.grid.getElevators().length; eID++) {
                if (this.grid.getElevators()[eID].getMovementDirection() == ElevatorMovement.STAND_STILL && this.grid.elevators[eID].doorOpen)
                    for (Passenger p : this.grid.getElevators()[eID].getPassengers()
                    ) {
                        Floor f = this.grid.getFloorClosestToElevator(eID);
                        if (p.getDestination().equals(f)) {
                            if (this.grid.isElevatorinFloor(eID, p.destination.id)) {
                                p.setActivity(Activity.HAS_ARRIVED);
                                removeFromElevator.add(p);
                                this.gui.showDestination(false, eID, f.id);
                            }
                        }
                    }
                this.grid.getElevators()[eID].getPassengers().removeAll(removeFromElevator);
                removeFromElevator.clear();
            }
        }
    }

    /**
     * baut eine Verbindung per serieller Schnittstelle auf und fuegt dieser einen Data-Listener hinzu, welcher
     * auf das Zeichen'\n' hoert
     */
    public boolean initConnection() {
        boolean open = serialPort.openPort();
        if (open) {
            MessageListener listener = new MessageListener(this);
            serialPort.addDataListener(listener);
            serialPort.setBaudRate(115200);
            isConnected = true;
            logInfo("connected to: " + this.getSerialPort().getDescriptivePortName());
            //printSerialPortSettings();
        } else {
            logWarn("couldn't open the serial port: " + serialPort.getDescriptivePortName());
        }
        return open;
    }


    /**
     * sendet einen Befehl ueber die serielle Schnittstelle
     *
     * @param cmd Befehl
     */
    public void sendCommand(String cmd) {
        byte[] temp = cmd.getBytes();
        //System.out.println(Arrays.toString(temp));
        serialPort.writeBytes(temp, temp.length);
    }

    public void closeConnection() {
        if (serialPort != null) {
            serialPort.closePort();
            if (this.sTime != null && this.sTime.timer != null)
                this.sTime.timer.cancel();
            this.grid = null;
            this.commandState.init_done = false;
            logInfo("closed connection with: " + this.getSerialPort().getDescriptivePortName());
            serialPort = null;
            isConnected = false;
        }
    }

    /**
     * Fuegt einen Fahrgast zu einem Stockwerk hinzu
     *
     * @param origin      wo kommt der Fahrgast her
     * @param destination wo der Fahrgast hin will
     */
    public void addPassenger(Floor origin, Floor destination) {
        //Fahrgaeste starten immer in einem Stockwerk
        Passenger newPassenger = new Passenger(this.grid.floors[origin.getId()], this.grid.floors[destination.getId()]);
        this.grid.floors[origin.getId()].getPassengers().add(newPassenger);
        //Fahrgast drueckt Knopf zum Aufzug rufen
        this.out.add("BUTTON_PUSH " + origin.id + '\n');
    }

    /**
     * Klasse fuer das event-based reading fuer das Event: A delimited string-based message has been received
     * siehe <a href="https://fazecast.github.io/jSerialComm">...</a>
     */
    private static final class MessageListener implements SerialPortMessageListener {
        Logic logic;

        MessageListener(Logic logic) {
            this.logic = logic;
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
            //eingegangene Nachricht speichern (ohne '\n')
            logic.in.add(sb.delete(sb.length() - 1, sb.length()).toString());
        }
    }

    public void closeThreads() {
        LogManager.shutdown();
        if (sTime != null)
            sTime.timer.cancel();
    }

    public void logWarn(String message) {
        logger.error(message + '\n');
    }

    public void logInfo(String message) {
        logger.info(message + '\n');
    }

    public void printSerialPortSettings() {
        System.out.println("baud: " + serialPort.getBaudRate());
        System.out.println("buffersizeRead: " + serialPort.getDeviceReadBufferSize());
        System.out.println("buffersizeWrite: " + serialPort.getDeviceWriteBufferSize());
        System.out.println("stopbits: " + serialPort.getNumStopBits());
        System.out.println("flowcontroll: " + serialPort.getFlowControlSettings());
        System.out.println("databits: " + serialPort.getNumDataBits());
        System.out.println("parity: " + serialPort.getParity());
    }
}
