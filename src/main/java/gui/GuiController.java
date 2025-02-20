package gui;

import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import logic.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView<SerialPort> serialPane;
    @FXML
    private Button speed;
    @FXML
    private Button addDyn;
    @FXML
    private ScrollPane ePaneTest;
    @FXML
    private HBox eGrid;
    @FXML
    private Button timerButton;
    @FXML
    private Button reloadSerial;
    /**
     * Logik
     */
    private LogicWrapper logicWrapper = new LogicWrapper(this);
    boolean running = false;
    private static final int BUTTONS_PER_COLUMN_IN_ELEVATOR = 2;
    private static final double ELEVATOR_HEIGHT = 80;
    private static final double ELEVATOR_WIDTH = 60;
    private static final double ELEVATOR_WALL_THICKNESS = 5;
    private static final double BUTTON_BORDER_THICKNESS = 2;
    private static final double LINE_THICKNESS = 2;
    private static final double ELEVATOR_OFFSET = 100;
    private static final int BUTTON_COLUMNS_UNDER_ELEVATOR = 2;
    private static final double BUTTON_WIDTH = ELEVATOR_WIDTH / BUTTON_COLUMNS_UNDER_ELEVATOR;
    private final double ELEVATOR_SPACE_BETWEEN = 10;
    public final int ELEVATOR_SPEED = 20;

    private Group allElevators = new Group();

    private Group allElevatorDoors = new Group();
    private Group allFloors = new Group();

    private Group gridAll = new Group();
    private List<Group> elevators = new ArrayList<>();
    private List<Group> elevatorDoors = new ArrayList<>();
    private List<Group> floors = new ArrayList<>();

    private List<Label> passengerElevator = new ArrayList<>();
    private List<Label> passengerFloor = new ArrayList<>();
    private final List<List<Rectangle>> floorbuttons = new ArrayList<>();
    private final List<List<Rectangle>> elevatorbuttons = new ArrayList<>();
    AnimationTimer a = new AnimationTimer() {
        long prev = 0;

        @Override
        public void handle(long l) {
            // 0.01666 60FPS
            double elapsedTime = ((l - prev) / 1000000000.0);
            if (elapsedTime > 0.05) {
                elapsedTime = 0.016666;
            }
            prev = l;
            //speed.setScaleY(speed.getScaleY() + xD);

            //Verarbeitung auf der Logikseite fuer die vergangene Zeit seit dem letzten Aufruf
            logicWrapper.tickUpdate(elapsedTime);

            //dynamische Objekte wie die Aufzuege veraendern. Farbe und Position
            if (logicWrapper.getCommandState().init_done) {
                changeDynamicObjects();
            }
        }
    };


    public void changeDynamicObjects() {
        for (int i = 0; i < logicWrapper.getLogic().getGrid().getElevators().length; i++) {
            elevators.get(i).setTranslateY(logicWrapper.getLogic().getGrid().getElevators()[i].getElevation() * -1);
            passengerElevator.get(i).setText(logicWrapper.getLogic().getGrid().getElevators()[i].getPassengers().size() + "");
        }
        timerButton.setText(logicWrapper.getLogic().currentTime());
        for (int i = 0; i < logicWrapper.getLogic().getGrid().getFloors().length; i++) {
            passengerFloor.get(i).setText(logicWrapper.getLogic().getGrid().getFloors()[i].getPassengers().size() + "");
        }
    }

    public void changeElevatorButtonLight(boolean on, int elevatorID, int buttonID) {
        if (on) {
            elevatorbuttons.get(elevatorID).get(buttonID).setFill(Color.YELLOW);
        } else {
            elevatorbuttons.get(elevatorID).get(buttonID).setFill(Color.GRAY);
        }
    }

    public void changeFloorButtonLight(boolean on, int floorID, int buttonID) {
        if (on) {
            floorbuttons.get(floorID).get(buttonID).setFill(Color.YELLOW);
        } else {
            floorbuttons.get(floorID).get(buttonID).setFill(Color.WHITE);
        }
    }

    public void changeDoorOpen(boolean open, int elevatorID) {
        elevators.get(elevatorID).getChildren().get(1).setVisible(logicWrapper.getLogic().getGrid().getElevators()[elevatorID].isDoorOpen());
    }

    public void clearGrid() {
        ePaneTest.setContent(null);
        elevators.clear();
        floors.clear();
        elevatorbuttons.clear();
        floorbuttons.clear();
        gridAll = new Group();
    }

    public void drawGrid() {

        for (int i = 0; i < logicWrapper.getLogic().getGrid().floors.length; i++) {
            addFloor(i);
        }
        for (int i = 0; i < logicWrapper.getLogic().getGrid().elevators.length; i++) {
            addStaticElevator(i);
        }
        for (int i = 0; i < logicWrapper.getLogic().getGrid().elevators.length; i++) {
            addElevator(i);
        }
        /**       if (!running) {
         a.start();
         } else {
         logicWrapper.getLogic().clearThreads();
         a.stop();
         }

         running = !running;
         */
    }


    @FXML
    protected void toggleLoop() {
        setLoopRunning(!running);
    }

    public void setLoopRunning(boolean run) {
        if (!running && run) {
            a.start();
        } else if (running && !run) {
            a.stop();
        }
        if (this.logicWrapper.getLogic() != null)
            this.logicWrapper.getLogic().setTimerRunning(run);
        running = run;
    }


    @FXML
    protected void setSelectedSerialPort() {
        if (this.serialPane.getSelectionModel().getSelectedItem() != null) {
            this.setLoopRunning(true);
            this.logicWrapper.setSerialPort(this.serialPane.getSelectionModel().getSelectedItem());
            System.out.println("connected to: " + this.logicWrapper.getSerialPort().getDescriptivePortName());
            this.logicWrapper.initConnection();
        } else {
            System.out.println("no serial port selected");
        }
    }

    @FXML
    protected void addElevator(int elevatorNum) {
        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }
        gridAll.getChildren().add(createElevator(elevatorNum));
        ePaneTest.setContent(gridAll);

    }

    @FXML
    protected void addFloor(int floornum) {

        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }

        gridAll.getChildren().add(createFloor(floornum));
        gridAll.getChildren().add(createFloorLine(floornum));
        ePaneTest.setContent(gridAll);
    }

    @FXML
    protected void addStaticElevator(int elevatorNum) {

        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }
        gridAll.getChildren().add(createStaticElevator(elevatorNum));
        ePaneTest.setContent(gridAll);
    }

    private Group createElevator(int elevatorNum) {
        Rectangle outer = new Rectangle();
        outer.setFill(Color.BLACK);
        outer.setHeight(ELEVATOR_HEIGHT);
        outer.setWidth(ELEVATOR_WIDTH);
        Rectangle inner = new Rectangle();
        inner.setFill(Color.GREY);
        inner.setHeight(ELEVATOR_HEIGHT - ELEVATOR_WALL_THICKNESS * 2);
        inner.setWidth(ELEVATOR_WIDTH - ELEVATOR_WALL_THICKNESS * 2);
        //ePaneTest.getChildren().addAll(outer,inner);
        inner.setX(ELEVATOR_WALL_THICKNESS);
        inner.setY(ELEVATOR_WALL_THICKNESS);
        inner.setVisible(false);
        //outer.setX((elevatorNum + 1) * 100);
        //outer.setY(20);
        Group testMovable = new Group();


        testMovable.getChildren().addAll(outer, inner);
        int a = (logicWrapper.getLogic().getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        if (logicWrapper.getLogic().getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTONS_PER_COLUMN_IN_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;

        //Gap zu FloorButtons
        //((logicWrapper.getLogic().getMaxAmountButtons()+2)* BUTTON_WIDTH)

        testMovable.setTranslateX((ELEVATOR_WIDTH) * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum) + ELEVATOR_OFFSET + elevatorNum * ELEVATOR_SPACE_BETWEEN);
        elevators.add(testMovable);

        return testMovable;
    }

    private Group createStaticElevator(int elevatorNum) {
        Group staticElevator = new Group();
        Rectangle shaft = new Rectangle();
        shaft.setFill(Color.LIGHTGRAY);

        shaft.setHeight((logicWrapper.getLogic().getGrid().floors.length) * ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR);
        shaft.setHeight((logicWrapper.getLogic().getGrid().floors[logicWrapper.getLogic().getGrid().floors.length - 1].getHeight() + ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR));
        shaft.setWidth(LINE_THICKNESS);

        int a = (logicWrapper.getLogic().getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        if (logicWrapper.getLogic().getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;

        Label passengerAmount = new Label();
        passengerAmount.setTranslateY(200);
        passengerElevator.add(passengerAmount);
        passengerAmount.setTranslateX((ELEVATOR_WIDTH * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum)
                + ELEVATOR_OFFSET + ELEVATOR_WIDTH / 2 + elevatorNum * ELEVATOR_SPACE_BETWEEN) - LINE_THICKNESS / 2);
        passengerAmount.setText("0");
        shaft.setX((ELEVATOR_WIDTH * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum)
                + ELEVATOR_OFFSET + ELEVATOR_WIDTH / 2 + elevatorNum * ELEVATOR_SPACE_BETWEEN) - LINE_THICKNESS / 2);
        shaft.setY((logicWrapper.getLogic().getGrid().floors.length - 1) * ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR * -1);

        staticElevator.getChildren().addAll(passengerAmount, shaft);
        //create FloorButtons
        List<Rectangle> buttonsInElevator = new ArrayList<>();


        //System.out.println(logicWrapper.getLogic().getGrid().elevators[i].getButtons().size());
        for (int j = 0; j < logicWrapper.getLogic().getGrid().elevators[elevatorNum].getButtons().size(); j++) {
            Rectangle floorLCD_Border = new Rectangle();
            floorLCD_Border.setFill(Color.BLACK);
            floorLCD_Border.setHeight(BUTTON_WIDTH);
            floorLCD_Border.setWidth(BUTTON_WIDTH);
            Rectangle floorLCD = new Rectangle();
            floorLCD.setFill(Color.GRAY);
            floorLCD.setHeight(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            floorLCD.setWidth(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            Text test = new Text("" + j);
            test.setStyle("-fx-font: 16 arial;");
            StackPane test2 = new StackPane(floorLCD_Border, floorLCD, test);
            int columnLength = j / BUTTONS_PER_COLUMN_IN_ELEVATOR;
            test2.setLayoutX((ELEVATOR_WIDTH * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum) + ELEVATOR_OFFSET + BUTTON_WIDTH * columnLength) + elevatorNum * ELEVATOR_SPACE_BETWEEN);
            test2.setLayoutY((ELEVATOR_HEIGHT * 1.1 + j % BUTTONS_PER_COLUMN_IN_ELEVATOR * BUTTON_WIDTH));
            int floornum = j;
            test2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    //FOR MANUAL TESTING
                    System.out.println("REQUEST " + elevatorNum + " " + floornum);
                    if (logicWrapper.getLogic().grid.elevators[elevatorNum].getMovementDirection() == ElevatorMovement.STAND_STILL) {
                        logicWrapper.getLogic().grid.elevators[elevatorNum].setMovementDirection(ElevatorMovement.UP);
                    } else if (logicWrapper.getLogic().grid.elevators[elevatorNum].getMovementDirection() == ElevatorMovement.UP) {
                        logicWrapper.getLogic().grid.elevators[elevatorNum].setMovementDirection(ElevatorMovement.DOWN);
                    } else if (logicWrapper.getLogic().grid.elevators[elevatorNum].getMovementDirection() == ElevatorMovement.DOWN) {
                        logicWrapper.getLogic().grid.elevators[elevatorNum].setMovementDirection(ElevatorMovement.STAND_STILL);
                    }
                    logicWrapper.getLogic().grid.elevators[elevatorNum].setDoorOpen(
                            !logicWrapper.getLogic().grid.elevators[elevatorNum].isDoorOpen());
                }
            });
            staticElevator.getChildren().addAll(test2);
            buttonsInElevator.add(floorLCD);
        }


        elevatorbuttons.add(buttonsInElevator);
        //TODO more buttons
        //if(logic.priority_mode)
        return staticElevator;
    }


    private Group createFloor(int floornum) {
        Group allButtonsFloor = new Group();
        List<Rectangle> buttonsFloor = new ArrayList<>();
        for (int i = 0; i < logicWrapper.getLogic().getGrid().floors[floornum].getButtons().size(); i++) {
            Rectangle outer = new Rectangle();
            outer.setFill(Color.BLACK);
            outer.setHeight(BUTTON_WIDTH);
            outer.setWidth(BUTTON_WIDTH);
            Rectangle inner = new Rectangle();
            inner.setFill(Color.WHITE);
            inner.setHeight(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            inner.setWidth(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            StackPane test = new StackPane();
            test.setTranslateY((logicWrapper.getLogic().getGrid().floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
            test.setTranslateX(i * (BUTTON_WIDTH));
            Text text = new Text("" + logicWrapper.getLogic().getGrid().getFloors()[floornum].getButtons().get(i).getSymbol());
            test.getChildren().addAll(outer, inner, text);


            int fI = i;
            test.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    System.out.println("BUTTON_PUSH " + floornum + " " + fI);
                }
            });
            Label label = new Label("0");
            label.setTranslateX(-20);
            label.setTranslateY((logicWrapper.getLogic().getGrid().floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
            passengerFloor.add(label);
            allButtonsFloor.getChildren().addAll(test, label);
            buttonsFloor.add(inner);
        }
        floors.add(allButtonsFloor);
        floorbuttons.add(buttonsFloor);
        return allButtonsFloor;
    }

    private Rectangle createFloorLine(int floorNum) {
        Rectangle heightLine = new Rectangle();
        heightLine.setFill(Color.LIGHTGRAY);
        int a = logicWrapper.getLogic().getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR;
        if (logicWrapper.getLogic().getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;


        int elevatorNum = logicWrapper.getLogic().getGrid().elevators.length;

        heightLine.setWidth(((ELEVATOR_WIDTH * (double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * 1) + ELEVATOR_SPACE_BETWEEN) * elevatorNum - ELEVATOR_SPACE_BETWEEN);

        heightLine.setHeight(LINE_THICKNESS);

        heightLine.setY((logicWrapper.getLogic().getGrid().floors[floorNum].getHeight()) * -1 + ELEVATOR_HEIGHT);
        heightLine.setX(ELEVATOR_OFFSET);
        return heightLine;
    }

    @FXML
    protected void toggleTimerButton() {
        if (this.running)
            logicWrapper.getLogic().setTimerRunning(!logicWrapper.getLogic().isTimer_isrunning());
    }


    @FXML
    protected void showSerialPorts() {
        this.serialPane.getItems().clear();
        this.serialPane.getItems().addAll(SerialPort.getCommPorts());

    }

    @FXML
    protected void addPassenger() {
        logicWrapper.getLogic().addPassenger(1, 0);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        showSerialPorts();
        //TODO verschieben nach connect button

        //Darstellung der SerialPorts aendern
        this.serialPane.setCellFactory(e -> new ListCell<SerialPort>() {
            @Override
            protected void updateItem(SerialPort sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null || sp.getDescriptivePortName() == null) {
                    setText("");
                } else {
                    setText(sp.getDescriptivePortName());
                }
            }
        });

    }

    @FXML
    protected void testGrid() {

        logicWrapper.getCommandState().parse("init_base 3 5 12 5 6");
        logicWrapper.getCommandState().parse("init_done");
        logicWrapper.getCommandState().parse("light ON e 1 0");
        logicWrapper.getCommandState().parse("light ON e 3 2");
        logicWrapper.getCommandState().parse("open 2");


    }
}