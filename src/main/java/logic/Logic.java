package logic;

import com.fazecast.jSerialComm.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Logic {
    public ElevatorGrid grid;
    Timer timer = new Timer("Uhrzeit");

    Queue<String> inbox;
    Queue<String> outbox;
    int hoursPerDay;
    int currentHour = 0;
    int currentMinute = 0;

    private boolean timer_isrunning = false;

    public Logic(int elevators, int floors, int hoursPerDay, int hour, int minute) {
        this.grid = new ElevatorGrid(elevators, floors);
        this.hoursPerDay = hoursPerDay;
        this.currentHour = hour;
        this.currentMinute = minute;
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

    public void toggleTimer() {
        if (timer_isrunning) {
            timer.cancel();
            return;
        }
        timer_isrunning = true;
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentMinute++;
                currentHour = (currentHour + (currentMinute / 60)) % hoursPerDay;
                currentMinute %= 60;
                System.out.println(currentHour + ":" + currentMinute);
            }
        }, 1000, 1000);
    }


    public String currentTime() {
        String formatted = String.format("%02d:%02d", currentHour, currentMinute);
        return formatted;
    }

    public void clearThreads() {
        if (timer_isrunning) {
            timer.cancel();
        }
    }
}
