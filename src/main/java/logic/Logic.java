package logic;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import gui.GuiController;
import lombok.Getter;
import lombok.Setter;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Setter
@Getter
public class Logic {

    ElevatorGrid grid;

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

    SimTime sTime;
    private final CommandState commandState = new CommandState(this);

    public Logic(GuiController gui) {
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
            for (Elevator e : this.grid.getElevators()
            ) {

                if ((e.getElevation() + grid.getBufferHeight()) < grid.floors[0].getHeight() && !e.encounteredError) {
                    e.encounteredError = true;
                    this.grid.elevators[e.id].setMovementDirection(ElevatorMovement.STAND_STILL);
                    this.grid.bufferHeight += 0.5;
                    this.gui.displayError(
                            "elevator " + e.getId() + " reached the lowest point but has not stopped."
                            , e.getId());
                } else if ((e.getElevation() - grid.getBufferHeight()) > grid.floors[grid.floors.length - 1].getHeight() && !e.encounteredError) {
                    e.encounteredError = true;
                    this.grid.elevators[e.id].setMovementDirection(ElevatorMovement.STAND_STILL);
                    this.grid.bufferHeight += 0.5;
                    this.gui.displayError(
                            "elevator " + e.getId() + " reached the highest point but has not stopped."
                            , e.getId());
                }
                if (!e.encounteredError) {
                    e.setSpeed(elapsedTime * this.gui.ELEVATOR_SPEED);
                    e.updateElevation();
                }
            }
            for (Elevator e : this.getGrid().getElevators()
            ) {
                if (!(e.getMovementDirection() == ElevatorMovement.STAND_STILL)) {

                    for (Floor f : this.grid.getFloors()
                    ) {
                        if (!f.equals(e.mostRecentFloor) && Math.abs(e.getElevation() - f.getHeight()) < elapsedTime * (this.gui.ELEVATOR_SPEED)) {
                            out.add("ARRIVE " + e.getId() + " " + f.getId() + "\n");
                            e.mostRecentFloor = f;
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
                        //TODO Fahrgast drueckt Knopf zum ZielFloor
                        for (int eID = 0; eID < this.grid.getElevators().length; eID++) {
                            if (this.grid.isElevatorinFloor(eID, fID)) {
                                if (p.getActivity() == Activity.IS_WAITING && this.grid.elevators[eID].doorOpen) {
                                    removeFromFloor.add(p);
                                    this.grid.getElevators()[eID].getPassengers().add(p);
                                    p.setActivity(Activity.IN_ELEVATOR);
                                    //Knopf fuer Zieletage im Aufzug druecken
                                    this.out.add(this.grid.getElevators()[eID].getButtons().get(
                                            this.grid.getElevators()[eID].getPassengers().indexOf(p)
                                    ).onClick);
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
                            if (this.grid.isElevatorinFloor(eID, f.id)) {
                                p.setActivity(Activity.HAS_ARRIVED);
                                removeFromElevator.add(p);
                            }
                        }
                    }
                this.grid.getElevators()[eID].getPassengers().removeAll(removeFromElevator);
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

    public void addPassenger(Floor origin, Floor destination) {
        //Fahrgaeste starten immer in einem Stockwerk
        Passenger newPassenger = new Passenger(this.grid.floors[origin.getId()], this.grid.floors[destination.getId()]);
        this.grid.floors[origin.getId()].getPassengers().add(newPassenger);
        //Fahrgast drueckt Knopf zum Aufzug rufen
        this.out.add(this.grid.floors[origin.getId()].buttons.get(0).onClick);
    }

    /**
     * Klasse fuer das event-based reading fuer das Event: A delimited string-based message has been received
     * siehe https://fazecast.github.io/jSerialComm/
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
}
