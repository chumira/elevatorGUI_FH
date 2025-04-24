package logic;

import logic.types.ElevatorMovement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public List<LEDButton> aggregateButtons() {
        List<LEDButton> res = new LinkedList<>();

        if (floorButtons != null && floorButtons.size() > 0)
            res.addAll(floorButtons);
        if (emergency != null)
            res.add(emergency);
        return res;
    }

}
