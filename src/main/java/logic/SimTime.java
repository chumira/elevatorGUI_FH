package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class SimTime {
    Timer timer;
    int hoursPerDay;
    int minutesPerHour;
    int currentHour = 0;
    int currentMinute = 0;
    int currentSpeed = 1000;
    private boolean timer_isrunning = false;
    private Logic parent;


    public SimTime(int hoursPerDay, int hour, int minute, Logic logic) {
        this.hoursPerDay = hoursPerDay;
        this.currentHour = hour;
        this.currentMinute = minute;
        this.minutesPerHour = 60;
        this.parent = logic;
    }

    /**
     * erstellt einen Timer mit einer hochzarhlenden Uhrzeit oder haelt die Uhrzeit an
     *
     * @param run      anhalten oder nicht
     * @param newSpeed die Zeitabstaende zwischen den Aufrufen
     */
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

    /**
     * erstellt einen TimerThread welcher eine Minute pro Ausfuehrung hochzaehlt
     *
     * @param newSpeed die Zeitabstaende zwischen den Aufrufen
     */
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

    public String currentTime() {
        return String.format("%02d:%02d", currentHour, currentMinute);
    }

    public void setCurrentTime(String hour, String minute) {
        currentHour = Integer.parseInt(hour);
        currentMinute = Integer.parseInt(minute);
    }
}
