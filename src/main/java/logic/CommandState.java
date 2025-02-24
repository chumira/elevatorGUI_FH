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
                case "INIT_BASE" -> {
                    init_phase = true;
                    init_done = false;
                    this.elevator_States.clear();
                    init_amountFloors = Integer.parseInt(div[1]);
                    init_amountElevators = Integer.parseInt(div[2]);
                    init_hoursPerDay = Integer.parseInt(div[3]);
                    init_hourAtStart = Integer.parseInt(div[4]);
                    init_minuteAtStart = Integer.parseInt(div[5]);
                }
                case "INIT_STATE" -> {
                    if (init_phase)
                        elevator_States.add(new Pair<>(Integer.parseInt(div[1]), Integer.parseInt(div[2])));
                }
                case "INIT_DONE" -> {
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
                    this.parent.gui.setLoopRunning(true);
                    this.parent.gui.clearGrid();
                    this.parent.gui.drawGrid();
                    this.parent.gui.showFloorList();
                    this.elevator_States.clear();
                }
                case "OPEN" -> {
                    if (init_done) {
                        //TODO error when elevator is moving
                        this.parent.logic.grid.elevators[Integer.parseInt(div[1])].doorOpen = true;
                        this.parent.gui.changeDoorOpen(true, Integer.parseInt(div[1]));
                    }
                }
                case "CLOSE" -> {
                    if (init_done) {
                        //TODO error when elevator is moving
                        this.parent.logic.grid.elevators[Integer.parseInt(div[1])].doorOpen = false;
                        this.parent.gui.changeDoorOpen(false, Integer.parseInt(div[1]));
                    }
                }
                case "MOVE_UP" -> {
                    //TODO error when elevatordoor is open
                    if (init_done)
                        this.parent.logic.grid.elevators[Integer.parseInt(div[1])].setMovementDirection(ElevatorMovement.UP);
                }
                case "MOVE_DOWN" -> {
                    //TODO error when elevatordoor is open
                    if (init_done)
                        this.parent.logic.grid.elevators[Integer.parseInt(div[1])].setMovementDirection(ElevatorMovement.DOWN);
                }
                case "STOP" -> {
                    if (init_done)
                        this.parent.logic.grid.elevators[Integer.parseInt(div[1])].setMovementDirection(ElevatorMovement.STAND_STILL);
                }
                case "LIGHT" -> {
                    //LIGHT ON|OFF F|E 0-N 0-M
                    if (init_done) {
                        String onOrOff = div[1];
                        String floorOrElevator = div[2];
                        int feID = Integer.parseInt(div[3]);
                        int bID = Integer.parseInt(div[4]);
                        boolean on;
                        if (onOrOff.equals("ON")) {
                            on = true;
                        } else if (onOrOff.equals("OFF")) {
                            on = false;
                        } else {
                            throw new IllegalArgumentException("expected '0N' or 'OFF' but got '" + div[1] + "'");
                        }
                        if (floorOrElevator.equals("E")) {
                            this.parent.logic.grid.elevators[feID].getButtons().get(bID).isGlowing = on;
                            this.parent.gui.changeElevatorButtonLight(on, feID, bID);
                        } else if (floorOrElevator.equals("F")) {
                            this.parent.logic.grid.floors[feID].getButtons().get(bID).isGlowing = on;
                            this.parent.gui.changeFloorButtonLight(on, feID, bID);
                        } else {
                            throw new IllegalArgumentException("expected 'E' or 'F' but got " + div[2] + "'");
                        }
                    }
                }
                case "PRINT" -> {
                    System.out.print("-->");
                    for (int i = 1; i < div.length; i++) {
                        System.out.print(div[i]);
                    }
                    System.out.println();
                }
                default -> throw new UnsupportedOperationException(div[0] + " is not a known command");
            }
        } catch (ArrayIndexOutOfBoundsException | UnsupportedOperationException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

}
