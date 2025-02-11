package logic;

import com.fazecast.jSerialComm.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Logic {
    public ElevatorGrid grid;
    Timer timer;

    Queue<String> inbox;
    Queue<String> outbox;
    int hoursPerDay;
    int currentHour = 0;
    int currentMinute = 0;


    private boolean timer_isrunning = false;

    public Logic(int floors, int elevators, int hoursPerDay, int hour, int minute) {
        this.grid = new ElevatorGrid(elevators, floors);
        this.hoursPerDay = hoursPerDay;
        this.currentHour = hour;
        this.currentMinute = minute;
        toggleTimer();
    }


    public void toggleTimer() {
        if (timer_isrunning) {
            timer.cancel();
            timer_isrunning = false;
            return;
        }
        timer_isrunning = true;
        timer = new Timer("uhrzeit");
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentMinute++;
                currentHour = (currentHour + (currentMinute / 60)) % hoursPerDay;
                currentMinute %= 60;
                if (currentMinute == 0) {
                    System.out.println("HOUR " + currentHour);
                }
            }
        }, 1000, 1000);
    }


    public String currentTime() {
        return String.format("%02d:%02d", currentHour, currentMinute);
    }

    public void clearThreads() {
        if (timer_isrunning) {
            timer.cancel();
        }
    }
}
