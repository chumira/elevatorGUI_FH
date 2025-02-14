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
    private int eCount = 0;
    private int fCount = 0;

    private SerialConnection serialConnection;


    private static final int BUTTONS_PER_COLUMN_IN_ELEVATOR = 4;
    public final int ELEVATOR_SPEED = 20;

    private Group allElevators = new Group();

    private Group allElevatorDoors = new Group();
    private Group allFloors = new Group();

    private Group gridAll = new Group();
    private List<Group> elevators = new ArrayList<>();
    private List<Group> elevatorDoors = new ArrayList<>();
    private List<Group> floors = new ArrayList<>();
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
        }
        timerButton.setText(logicWrapper.getLogic().currentTime());
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
            elevatorbuttons.get(floorID).get(buttonID).setFill(Color.YELLOW);
        } else {
            elevatorbuttons.get(floorID).get(buttonID).setFill(Color.WHITE);
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
        gridAll = new Group();
    }

    public void drawGrid() {

        //logicWrapper.setLogic(new Logic(4, 2, 24, 22, 54));
        for (int i = 0; i < logicWrapper.getLogic().getGrid().elevators.length; i++) {
            addElevator(i);
        }
        for (int i = 0; i < logicWrapper.getLogic().getGrid().floors.length; i++) {
            addFloor(i);
        }
        for (int i = 0; i < logicWrapper.getLogic().getGrid().elevators.length; i++) {
            addStaticElevator(i);
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
    protected void testGrid() {

        logicWrapper.getCommandState().parse("init_base 3 5 12 5 6");
        logicWrapper.getCommandState().parse("init_done");
        logicWrapper.getCommandState().parse("light_on e 1 0");
        logicWrapper.getCommandState().parse("light_on e 3 2");
        logicWrapper.getCommandState().parse("open 2");


    }

    @FXML
    protected void testCommport2() {
        //delete previous elements to draw
        ePaneTest.setContent(null);
        elevators.clear();
        floors.clear();
        gridAll = new Group();
        if (!running) {
            a.start();
        } else {
            logicWrapper.getLogic().clearThreads();
            a.stop();
        }
        running = !running;
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
        this.logicWrapper.getLogic().setTimerRunning(run);
        running = run;
    }


    @FXML
    protected void setSelectedSerialPort() {
        if (this.serialPane.getSelectionModel().getSelectedItem() != null) {
            this.logicWrapper.setSerialPort(this.serialPane.getSelectionModel().getSelectedItem());
            System.out.println("connected to: " + this.logicWrapper.getSerialPort().getDescriptivePortName());
            this.logicWrapper.initConnection();
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
        outer.setHeight(60f);
        outer.setWidth(40f);
        Rectangle inner = new Rectangle();
        inner.setFill(Color.GREY);
        inner.setHeight(50f);
        inner.setWidth(30f);
        //ePaneTest.getChildren().addAll(outer,inner);
        inner.setX((elevatorNum + 1) * 100 + 5);
        inner.setY(25);
        inner.setVisible(false);
        outer.setX((elevatorNum + 1) * 100);
        outer.setY(20);
        Group testMovable = new Group();


        testMovable.getChildren().addAll(outer, inner);


        elevators.add(testMovable);


        return testMovable;
    }

    private Group createStaticElevator(int elevatorNum) {
        Group staticElevator = new Group();
        Rectangle shaft = new Rectangle();
        shaft.setFill(Color.LIGHTGRAY);
        shaft.setHeight(floors.size() * 100);
        shaft.setWidth(2f);
        shaft.setX(((elevatorNum + 1) * 100 + 20));
        shaft.setY((floors.size() * 100 - 100) * -1);

        staticElevator.getChildren().addAll(shaft);
        //create FloorButtons
        List<Rectangle> buttonsInElevator = new ArrayList<>();
        for (int i = 0; i < logicWrapper.getLogic().getGrid().elevators[i].getButtons().size(); i++) {
            Rectangle floorLCD = new Rectangle();
            floorLCD.setFill(Color.GRAY);
            floorLCD.setHeight(16f);
            floorLCD.setWidth(16f);
            Text test = new Text("" + i);
            test.setStyle("-fx-font: 16 arial;");
            StackPane test2 = new StackPane(floorLCD, test);
            int columnLength = i / BUTTONS_PER_COLUMN_IN_ELEVATOR;
            test2.setLayoutX(((elevatorNum + 1) * 100 + 18 * columnLength));
            test2.setLayoutY((100 + i % BUTTONS_PER_COLUMN_IN_ELEVATOR * 18));
            int floornum = i;
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
        for (int i = 0; i < logicWrapper.getLogic().getGrid().floors[floornum].getButtons().size(); i++) {
            Rectangle outer = new Rectangle();
            outer.setFill(Color.BLACK);
            outer.setHeight(20f);
            outer.setWidth(20f);
            Rectangle inner = new Rectangle();
            inner.setFill(Color.WHITE);
            inner.setHeight(16f);
            inner.setWidth(16f);
            StackPane test = new StackPane();
            test.setTranslateY((floornum * 100) * -1 + 60);
            test.setTranslateX(i * 25);
            Text text = new Text("" + logicWrapper.getLogic().getGrid().getFloors()[floornum].getButtons().get(i).getSymbol());
            test.getChildren().addAll(outer, inner, text);
            int fI = i;
            test.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    System.out.println("BUTTON_PUSH " + floornum + " " + fI);
                }
            });
            allButtonsFloor.getChildren().add(test);
        }
        floors.add(allButtonsFloor);
        return allButtonsFloor;
    }

    private Rectangle createFloorLine(int floorNum) {
        Rectangle heightLine = new Rectangle();
        heightLine.setFill(Color.LIGHTGRAY);
        heightLine.setWidth(logicWrapper.getLogic().getGrid().elevators.length * 100);
        heightLine.setHeight(2f);
        heightLine.setY((floorNum * 100 - 20) * -1 + 60);
        heightLine.setX(70);
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
}