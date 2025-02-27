package logic;

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
    double speed = 0;
    boolean encounteredError = false;
    Floor mostRecentFloor;

    public Elevator(int id) {
        this.id = id;
        this.buttons = new ArrayList<>();
        this.passengers = new ArrayList<>();
        this.movementDirection = ElevatorMovement.STAND_STILL;
    }


    public void updateElevation() {
        switch (this.movementDirection) {
            case STAND_STILL:
                break;
            case UP:
                this.elevation += this.speed;
                break;
            case DOWN:
                this.elevation -= this.speed;
                break;
            default:
                throw new IllegalArgumentException();

        }
    }

}
