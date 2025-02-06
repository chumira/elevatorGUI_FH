package logic;

import com.fazecast.jSerialComm.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Logic {
    public ElevatorGrid grid;
    //Timer timer = new Timer("Uhrzeit");

    Queue<String> inbox;
    Queue<String> outbox;

    public Logic() {
        this.grid = new ElevatorGrid(3, 3);
    }

    public Logic(int elevators, int floors, int hoursPerDay, int hour, int minute) {

        this.grid = new ElevatorGrid(elevators, floors);
    }

    public void moveElevator(double amount) {
    }

    public List<String> establishSerialConnection() {
        SerialPort[] a = SerialPort.getCommPorts();
        for (SerialPort b : a
        ) {
            System.out.println(b.getDescriptivePortName());
        }
        List<String> res = Arrays.stream(a).map(SerialPort::getDescriptivePortName).toList();
        return res;
    }

    public void pause() {
    }

}
