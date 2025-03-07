package logic;

import javafx.util.Pair;
import logic.types.ElevatorMovement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    boolean mode_UpDown = false;
    boolean mode_Priority = false;
    boolean mode_EmergencyHalt = false;
    Logic parent;

    private static final Logger logger = LogManager.getLogger("commands");

    public CommandState(Logic parent) {
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
                    //TODO hide/remove old stuff
                }
                case "INIT_STATE" -> {
                    if (init_phase)
                        elevator_States.add(new Pair<>(Integer.parseInt(div[1]), Integer.parseInt(div[2])));
                    else throw new IllegalStateException();
                }
                case "MODE" -> {
                    if (init_phase) {
                        switch (div[1]) {
                            case "PRIORITY" -> {
                                mode_Priority = true;
                            }
                            case "EMERGENCY" -> {
                                mode_EmergencyHalt = true;
                            }
                            case "UPDOWN" -> {
                                mode_UpDown = true;
                            }
                            default ->
                                    throw new IllegalArgumentException("expected 'PRIORITY','UPDOWN' or 'EMERGENCY' but got '" + div[1] + "'");
                        }

                    } else throw new IllegalStateException("not in init_phase");
                }
                case "INIT_DONE" -> {
                    if (init_phase) {
                        init_phase = false;
                        this.parent.setGrid(new ElevatorGrid(init_amountFloors, init_amountElevators, mode_Priority, mode_EmergencyHalt, mode_UpDown));
                        this.parent.setSTime(new SimTime(init_hoursPerDay, init_hourAtStart, init_minuteAtStart, this.parent));
                        this.parent.gui.hideErrorMessage();
                        for (Pair<Integer, Integer> pair : this.elevator_States
                        ) {
                            this.parent.grid.adjustElevatorHeight(pair.getKey(), pair.getValue());
                        }
                        init_done = true;
                        this.parent.gui.setLoopRunning(true);
                        this.parent.gui.clearGrid();
                        this.parent.gui.drawGrid();
                        this.parent.gui.showFloorList();
                        this.elevator_States.clear();
                    } else throw new IllegalStateException("not in init_phase");

                }
                case "OPEN" -> {
                    if (init_done) {
                        int elevatorID = Integer.parseInt(div[1]);
                        if (this.parent.grid.elevators[elevatorID].getMovementDirection() != ElevatorMovement.STAND_STILL) {
                            this.parent.gui.displayError("Elevator " + elevatorID + " opened Door while moving.", elevatorID);
                            this.parent.logWarn("Elevator " + elevatorID + " opened Door while moving.");
                            this.parent.grid.elevators[elevatorID].setEncounteredError(true);
                        }
                        this.parent.grid.elevators[elevatorID].doorOpen = true;
                        this.parent.gui.changeDoorOpen(elevatorID);

                    } else throw new IllegalStateException("not initialized");
                }
                case "CLOSE" -> {
                    if (init_done) {
                        this.parent.grid.elevators[Integer.parseInt(div[1])].doorOpen = false;
                        this.parent.gui.changeDoorOpen(Integer.parseInt(div[1]));
                    } else throw new IllegalStateException("not initialized");
                }
                case "MOVE_UP" -> {
                    if (init_done) {
                        int elevatorID = Integer.parseInt(div[1]);
                        if (this.parent.grid.elevators[elevatorID].isDoorOpen()) {
                            this.parent.grid.elevators[elevatorID].setEncounteredError(true);
                            this.parent.gui.displayError("Elevator " + elevatorID + " started moving while door was open.", elevatorID);
                            this.parent.logWarn("Elevator " + elevatorID + " started moving while door was open.");
                        }
                        this.parent.grid.elevators[elevatorID].setMovementDirection(ElevatorMovement.UP);
                    } else
                        throw new IllegalStateException("not initialized");

                }
                case "MOVE_DOWN" -> {

                    if (init_done) {
                        int elevatorID = Integer.parseInt(div[1]);
                        if (this.parent.grid.elevators[elevatorID].isDoorOpen()) {
                            this.parent.grid.elevators[elevatorID].setEncounteredError(true);
                            this.parent.gui.displayError("Elevator " + elevatorID + " started moving while door was open.", elevatorID);
                            this.parent.logWarn("Elevator " + elevatorID + " started moving while door was open.");

                        }
                        this.parent.grid.elevators[elevatorID].setMovementDirection(ElevatorMovement.DOWN);

                    } else
                        throw new IllegalStateException("not initialized");

                }
                case "STOP" -> {
                    if (init_done)
                        this.parent.grid.elevators[Integer.parseInt(div[1])].setMovementDirection(ElevatorMovement.STAND_STILL);
                    else throw new IllegalStateException("not initialized");
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
                            this.parent.gui.displayErrorMessage(" expected 'ON' or 'OFF' but got '" + div[1] + "' for command " + div[0] + '\n');
                            throw new IllegalArgumentException("expected '0N' or 'OFF' but got '" + div[1] + "'");
                        }
                        if (floorOrElevator.equals("E")) {
                            //this.parent.logic.grid.elevators[feID].getButtons().get(bID).isGlowing = on;
                            this.parent.gui.changeElevatorButtonLight(on, feID, bID);
                        } else if (floorOrElevator.equals("F")) {
                            this.parent.grid.floors[feID].getButtons().get(bID).isGlowing = on;
                            this.parent.gui.changeFloorButtonLight(on, feID, bID);
                        } else {
                            throw new IllegalArgumentException("expected 'E' or 'F' but got '" + div[2] + "'");
                        }
                    } else
                        throw new IllegalStateException("not initialized");

                }
                case "PRINT" -> {
                    System.out.print("-->");
                    for (int i = 1; i < div.length; i++) {
                        System.out.print(div[i]);
                    }
                }
                default -> {

                    throw new UnsupportedOperationException();
                }
            }
        } catch (UnsupportedOperationException e) {
            this.parent.gui.displayErrorMessage(div[0] + " is not a valid command." + '\n');
            logger.error(div[0] + " is not a valid command.\n");
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            this.parent.gui.displayErrorMessage(div[0] + "->" + e.getMessage() + '\n');
            logger.error(div[0] + ": " + e.getMessage() + '\n');
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("false")) {
                this.parent.gui.displayErrorMessage(div[0] + " but was " + e.getMessage() + '\n');
                logger.error(div[0] + " but was " + e.getMessage() + '\n');
            }
        }
    }

}
