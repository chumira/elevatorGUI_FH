package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Elevator {
    String id;
    List<Passenger> passengers;
    List<LCDButton> buttons;
    boolean doorClosed, isMoving;
    double elevation = 0;
    double speed = 0.1;
    double closedpercentage;

    public Elevator(String id) {
        this.id = id;
        this.buttons = new ArrayList<>();
    }


    public void moveUp() {
        this.elevation -= this.speed;
        this.isMoving = true;
    }

    public void moveDown() {
        this.elevation += this.speed;
        this.isMoving = true;
    }

    public void stop() {
        this.isMoving = false;
    }
}
