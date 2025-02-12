package logic;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class CommandState {

    private boolean init_phase = false;

    public boolean init_done = false;
    int init_amountFloors;
    int init_amountElevators;

    int init_hoursPerDay;
    int init_hourAtStart;
    int init_minuteAtStart;

    List<Pair<Integer, Integer>> elevator_States = new LinkedList<>();

    LogicWrapper parent;


    public CommandState(LogicWrapper parent) {
        this.parent = parent;

    }

    public void parse(String a) {
        String aa = a.toUpperCase();
        String[] div = aa.split(" ");
        try {
            switch (div[0]) {
                case "INIT_START":
                    init_phase = true;
                    init_done = false;
                    break;
                case "INIT_BASE":
                    init_amountFloors = Integer.parseInt(div[1]);
                    init_amountElevators = Integer.parseInt(div[2]);
                    init_hoursPerDay = Integer.parseInt(div[3]);
                    init_hourAtStart = Integer.parseInt(div[4]);
                    init_minuteAtStart = Integer.parseInt(div[5]);
                    break;
                case "INIT_STATE":
                    elevator_States.add(new Pair<>(Integer.parseInt(div[1]), Integer.parseInt(div[2])));
                    break;
                case "INIT_DONE":
                    init_phase = false;
                    this.parent.setLogic(
                            new Logic(init_amountFloors, init_amountElevators,
                                    init_hoursPerDay, init_hourAtStart, init_minuteAtStart)
                    );
                    for (Pair<Integer, Integer> pair : this.elevator_States
                    ) {
                        this.parent.getLogic().getGrid().adjustElevatorHeight(pair.getKey(), pair.getValue());
                    }
                    init_done = true;
                    this.parent.getGui().drawGrid();
                    break;
                case "OPEN":
                    this.parent.logic.grid.elevators[Integer.parseInt(div[1])].doorOpen = true;
                    break;
                case "CLOSE":
                    this.parent.logic.grid.elevators[Integer.parseInt(div[1])].doorOpen = false;
                    break;
                default:
                    throw new UnsupportedOperationException();

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("couldnt parse the command");
        } catch (NumberFormatException e) {
            System.err.println("couldnt correctly parse as a number");
        }
    }

}
