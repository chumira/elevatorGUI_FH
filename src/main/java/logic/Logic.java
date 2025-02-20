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
    int hoursPerDay;
    int currentHour = 0;
    int currentMinute = 0;
    private boolean timer_isrunning = false;

    public Logic(int floors, int elevators, int hoursPerDay, int hour, int minute) {
        this.grid = new ElevatorGrid(elevators, floors);
        this.hoursPerDay = hoursPerDay;
        this.currentHour = hour;
        this.currentMinute = minute;
        //toggleTimer();
    }


    public void setTimerRunning(boolean run) {
        if (timer_isrunning && !run) {
            timer.cancel();
            timer_isrunning = false;
        } else if (!timer_isrunning && run) {
            timer_isrunning = true;

            timer = new Timer("uhrzeit");
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    currentMinute++;
                    if (hoursPerDay != 0)
                        currentHour = (currentHour + (currentMinute / 60)) % hoursPerDay;
                    currentMinute %= 60;
                    if (currentMinute == 0) {
                        System.out.println("HOUR " + currentHour);
                    }
                }
            }, 1000, 1000);
        }
    }


    public void addPassenger(int origin, int destination) {
        //Fahrgaeste starten immer in einem Stockwerk
        Passenger newPassenger = new Passenger(this.grid.floors[origin], this.grid.floors[destination]);
        this.grid.floors[origin].getPassengers().add(newPassenger);
    }

    public int getMaxAmountButtons() {
        int max = 0;
        for (Floor f : grid.floors) {
            max = Math.max(f.getButtons().size(), max);
        }
        return max;
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
