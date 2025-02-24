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
    int minutesPerHour;
    int currentHour = 0;
    int currentMinute = 0;
    int currentSpeed = 1000;
    private boolean timer_isrunning = false;
    private LogicWrapper parent;

    public Logic(int floors, int elevators, int hoursPerDay, int hour, int minute, LogicWrapper parent) {
        this.grid = new ElevatorGrid(elevators, floors);
        this.hoursPerDay = hoursPerDay;
        this.currentHour = hour;
        this.currentMinute = minute;
        this.minutesPerHour = 60;
        this.parent = parent;
    }


    public void setTimerRunning(boolean run, int newSpeed) {
        //stop timer
        if (timer_isrunning && !run) {
            timer.cancel();
            timer_isrunning = false;
        } else
            //start timer
            if ((!timer_isrunning && run)) {
                timer_isrunning = true;
                currentSpeed = newSpeed;
                timer = new Timer("uhrzeit");
                scheduleTimer(currentSpeed);
            } else
                //change timerSpeed
                if (currentSpeed != newSpeed) {
                    currentSpeed = newSpeed;
                    if (timer_isrunning) {
                        timer.cancel();
                        timer = new Timer("uhrzeit");
                        scheduleTimer(currentSpeed);
                    }
                }
    }

    public void scheduleTimer(int newSpeed) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentMinute++;
                if (hoursPerDay != 0)
                    currentHour = (currentHour + (currentMinute / minutesPerHour)) % hoursPerDay;
                currentMinute %= minutesPerHour;
                if (currentMinute == 0) {
                    //System.out.println("HOUR " + currentHour);
                    parent.out.add("HOUR " + currentHour + "\n");
                }
            }
        }, currentSpeed, currentSpeed);
    }


    public void addPassenger(Floor origin, Floor destination) {
        //Fahrgaeste starten immer in einem Stockwerk
        //TODO Fahrgast drueckt Knopf zum Aufzug rufen
        Passenger newPassenger = new Passenger(this.grid.floors[origin.getId()], this.grid.floors[destination.getId()]);
        this.grid.floors[origin.getId()].getPassengers().add(newPassenger);

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

    public void setCurrentTime(String hour, String minute) {
        currentHour = Integer.parseInt(hour);
        currentMinute = Integer.parseInt(minute);
    }

    public void clearThreads() {
        if (timer_isrunning) {
            timer.cancel();
        }
    }
}
