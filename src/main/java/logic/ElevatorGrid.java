package logic;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ElevatorGrid {
    private static final double HEIGHT_DIFFERENCE_FOR_EQUAL = 0.5;

    public static final double HEIGHT_INCREASE_PER_FLOOR = 100;
    public Floor[] floors;
    public Elevator[] elevators;

    ElevatorGrid(int amountElevators, int amountFloors) {
        floors = new Floor[amountFloors];
        elevators = new Elevator[amountElevators];
        for (int i = 0; i < amountElevators; i++) {
            elevators[i] = new Elevator(i);
            elevators[i].setElevation(0);
        }
        for (int i = 0; i < amountFloors; i++) {
            floors[i] = new Floor(i);
            floors[i].setHeight(i * 100);
        }
        for (int i = 0; i < amountElevators; i++) {
            for (int j = 0; j < amountFloors; j++) {
                elevators[i].getButtons().add(new LCDButton("" + j, "TODO"));
                //elevators[i].getButtons().get(j);
            }
        }
        for (int i = 0; i < amountFloors; i++) {
            floors[i].getButtons().add(new LCDButton("E" + i, "TODO"));
            //floors[i].setHeight(i * 100);
        }
        //for debug
        //floors[0].getButtons().add(new LCDButton("ABC", "TODO"));
    }

    public void adjustElevatorHeight(int elevatorID, int floorID) {
        elevators[elevatorID].setElevation(floors[floorID].getHeight());
    }

    public void adjustElevatorHeights(List<Pair<Integer, Integer>> toAdjust) {
        for (Pair<Integer, Integer> e : toAdjust) {
            adjustElevatorHeight(e.getKey(), e.getValue());
        }
    }

    public boolean isElevatorinFloor(int elevatorID, int floorID) {
        //sind Aufzug und Etage ungefaehr auf einer Hoehe?
        if (Math.abs(elevators[elevatorID].elevation - floors[floorID].height) < HEIGHT_DIFFERENCE_FOR_EQUAL) {
            //Bewegt sich der Aufzug?
            if (elevators[elevatorID].movementDirection == ElevatorMovement.STAND_STILL) {
                return true;
            }
        }
        return false;
    }

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
}
