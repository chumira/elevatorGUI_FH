/*
 * Author:  Jonas Harmuth
 */
package logic;

import javafx.util.Pair;
import logic.types.ElevatorMovement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Das Grid bestehend aus allen Aufzuegen und Etagen
 */
@Getter
@Setter
public class ElevatorGrid {
    private static final double HEIGHT_DIFFERENCE_FOR_EQUAL = 2;
    private static final double BUFFER_HEIGHT_DIFFERENCE = 0.5;
    public static final double INIT_ELEVATOR_HEIGHT = 0;
    public static final double HEIGHT_INCREASE_PER_FLOOR = 100;
    public final double ELEVATOR_SPEED = 20;
    public double bufferHeight = 10;
    public Floor[] floors;
    public Elevator[] elevators;

    ElevatorGrid(int amountFloors, int amountElevators) {
        this(amountFloors, amountElevators, false, false, false);
    }

    /**
     * Hauptconstructor fuer das Grid
     * @param amountFloors Anzahl Etagen
     * @param amountElevators Anzahl Aufzuege
     * @param priority Modus Prioritaet
     * @param emergency Modus Nothalt
     * @param updown Modus Updown
     */
    ElevatorGrid(int amountFloors, int amountElevators, boolean priority, boolean emergency, boolean updown) {
        floors = new Floor[amountFloors];
        elevators = new Elevator[amountElevators];
        //alle noetigen Aufzuege, Etagen und Buttons erstellen
        for (int i = 0; i < amountElevators; i++) {
            elevators[i] = new Elevator(i);
            elevators[i].setElevation(INIT_ELEVATOR_HEIGHT);
        }
        for (int i = 0; i < amountFloors; i++) {
            floors[i] = new Floor(i);
            floors[i].setHeight(i * HEIGHT_INCREASE_PER_FLOOR);
        }
        for (int i = 0; i < amountElevators; i++) {
            for (int j = 0; j < amountFloors; j++) {
                elevators[i].getFloorButtons().add(new LEDButton("" + j, "REQUEST " + elevators[i].getId() + " " + floors[j].getId() + "\n"));
            }
        }
        //falls Modi an oder aus sind muessen Buttons erstellt werden
        if (!updown) {
            for (int i = 0; i < amountFloors; i++) {
                floors[i].callButton = new LEDButton("E" + i, "BUTTON_PUSH " + floors[i].getId() + "\n");
            }
        } else {
            for (int i = 0; i < amountFloors; i++) {
                floors[i].updownButtons = new ArrayList<>();
                floors[i].updownButtons.add(new LEDButton("↑", "BUTTON_UP " + floors[i].getId() + "\n"));
                floors[i].updownButtons.add(new LEDButton("↓", "BUTTON_DOWN " + floors[i].getId() + "\n"));

            }
            //other modes like  priority, emergency buttons here
        }
        if (emergency) {
            for (int i = 0; i < amountFloors; i++) {
                floors[i].emergency = new LEDButton("N", "EMERGENCY_STOP_FLOOR " + floors[i].getId() + "\n");
            }
            for (int i = 0; i < amountElevators; i++) {
                elevators[i].emergency = new LEDButton("N", "EMERGENCY_STOP_ELEVATOR " + elevators[i].getId() + "\n");
            }
        }

    }

    /**
     * setzt die Hoehe des Aufzugs auf die Hoeher der Etage
     *
     * @param elevatorID der Aufzug
     * @param floorID    die Etage
     */
    public void adjustElevatorHeight(int elevatorID, int floorID) {
        elevators[elevatorID].setElevation(floors[floorID].getHeight());
    }

    public void adjustElevatorHeights(List<Pair<Integer, Integer>> toAdjust) {
        for (Pair<Integer, Integer> e : toAdjust) {
            adjustElevatorHeight(e.getKey(), e.getValue());
        }
    }

    /**
     * checkt ob der Aufzug ungefaehr auf Hoehe der Etage ist und stationaer ist
     *
     * @param elevatorID der Aufzug
     * @param floorID    die Etage
     * @return true falls Aufzugshoehe == Etagenhoehe
     */
    public boolean isElevatorinFloor(int elevatorID, int floorID) {
        //sind Aufzug und Etage ungefaehr auf einer Hoehe?
        if (Math.abs(elevators[elevatorID].elevation - floors[floorID].height) < HEIGHT_DIFFERENCE_FOR_EQUAL + BUFFER_HEIGHT_DIFFERENCE) {
            //Bewegt sich der Aufzug?
            return elevators[elevatorID].movementDirection == ElevatorMovement.STAND_STILL;
        }
        return false;
    }

    /**
     * checkt ob der Aufzug ungefaehr auf Hoehe der Etage ist
     *
     * @param elevatorID der Aufzug
     * @param floorID    die Etage
     * @return true falls Aufzugshoehe == Etagenhoehe
     */
    public boolean isElevatorRoughlyInFloor(int elevatorID, int floorID) {

        return Math.abs(elevators[elevatorID].elevation - floors[floorID].height) < (HEIGHT_DIFFERENCE_FOR_EQUAL);
    }

    public boolean isElevatorNotInFloor(int elevatorID, int floorID) {
        return Math.abs(elevators[elevatorID].elevation - floors[floorID].height) > (HEIGHT_DIFFERENCE_FOR_EQUAL);
    }

    /**
     * @param elevatorID der Aufzug
     * @return die Etage die dem Aufzug am naehsten ist
     */
    public Floor getFloorClosestToElevator(int elevatorID) {
        int floorID = -1;
        double distance = Double.MAX_VALUE;
        for (int fID = 0; fID < this.getFloors().length; fID++) {
            double current_distance = Math.abs(this.getFloors()[fID].height - this.getElevators()[elevatorID].elevation);
            if (current_distance < distance) {
                floorID = fID;
                distance = current_distance;
            }
        }
        return this.getFloors()[floorID];
    }

    /**
     * @return max. Anzahl von Knoepfen aller Etagen
     */
    public int getMaxAmountButtons() {
        int max = 0;
        for (Floor f : floors) {
            max = Math.max(f.aggregateButtons().size(), max);
        }
        return max;
    }
}
