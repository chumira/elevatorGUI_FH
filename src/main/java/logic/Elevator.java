package logic;

import logic.types.ElevatorMovement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Elevator {
    int id;
    List<Passenger> passengers;
    List<LCDButton> buttons;
    boolean doorOpen;
    ElevatorMovement movementDirection;
    double elevation = 0;
    boolean encounteredError = false;
    Floor mostRecentFloor;

    public Elevator(int id) {
        this.id = id;
        this.buttons = new ArrayList<>();
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

}
