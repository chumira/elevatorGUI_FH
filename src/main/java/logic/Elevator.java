/*
 * Author:  Jonas Harmuth
 */
package logic;

import logic.types.ElevatorMovement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Representation eines Aufzuges
 */
@Getter
@Setter
public class Elevator {
    int id;
    List<Passenger> passengers;
    List<LEDButton> floorButtons;

    LEDButton emergency;
    boolean doorOpen;
    ElevatorMovement movementDirection;
    double elevation = 0;
    boolean encounteredError = false;
    Floor mostRecentFloor;

    boolean inBetweenFloors = false;

    public Elevator(int id) {
        this.id = id;
        this.floorButtons = new ArrayList<>();
        this.passengers = new ArrayList<>();
        this.movementDirection = ElevatorMovement.STAND_STILL;
    }

    /**
     * Die Elevation des Fahrstuhls anpassen je nach Fahrtrichtung
     * @param amount wie viel sich bewegt werden soll
     */
    public void updateElevation(double amount) {
        switch (this.movementDirection) {
            case STAND_STILL:
                break;
            case UP:
                this.elevation += amount;
                break;
            case DOWN:
                this.elevation -= amount;
                break;
            default:
                throw new IllegalArgumentException();

        }
    }

    /**
     * fasst alle Buttons des Aufzuges zu einer Liste zusammen
     * @return Liste mit allen Buttons
     */
    public List<LEDButton> aggregateButtons() {
        List<LEDButton> res = new LinkedList<>();

        if (floorButtons != null && !floorButtons.isEmpty())
            res.addAll(floorButtons);
        if (emergency != null)
            res.add(emergency);
        return res;
    }

}
