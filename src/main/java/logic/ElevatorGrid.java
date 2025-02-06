package logic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElevatorGrid {

    public Floor[] floors;
    public Elevator[] elevators;

    ElevatorGrid(int amountElevators, int amountFloors) {
        floors = new Floor[amountFloors];
        elevators = new Elevator[amountElevators];
        for (int i = 0; i < amountElevators; i++) {
            elevators[i] = new Elevator(i + "");

        }
        for (int i = 0; i < amountFloors; i++) {
            floors[i] = new Floor(i + "");
        }

        for (int i = 0; i < amountElevators; i++) {
            for (int j = 0; j < amountFloors; j++) {
                elevators[i].getButtons().add(new LCDButton("" + j, "TODO"));
            }

        }
    }

}
