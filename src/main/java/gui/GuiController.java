package gui;

import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.*;

public class GuiController implements Initializable {
    @FXML
    private ListView<SerialPort> serialPane;

    @FXML
    private ListView<Floor> passengerFrom;
    @FXML
    private ListView<Floor> passengerTo;
    @FXML
    private ListView<Integer> clockSpeed;
    @FXML
    private ScrollPane ePaneTest;
    @FXML
    private HBox eGrid;
    @FXML
    private Button timerButton;
    @FXML
    private Button connectButton;

    @FXML
    private Button testErrorHide;
    @FXML
    private Button pauseButton;

    @FXML
    private Label time;
    @FXML
    private TextField clockHourField;
    @FXML
    private TextField clockMinuteField;
    @FXML
    private TextArea errorMessage;
    /**
     * Logik
     */
    private final Logic logic = new Logic(this);
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
    private final int ELEVATORERROR_GROUP_NUMBER = 0;

    private final int ELEVATORCASE_GROUP_NUMBER = 1;
    private final int ELEVATORDOOR_GROUP_NUMBER = 2;
    private Group gridAll = new Group();
    private final List<Group> elevators = new ArrayList<>();
    private final List<Group> floors = new ArrayList<>();

    private final List<Label> passengerElevator = new ArrayList<>();
    private final List<Label> passengerFloor = new ArrayList<>();
    private final List<List<Rectangle>> floorbuttons = new ArrayList<>();
    private final List<List<Rectangle>> elevatorbuttons = new ArrayList<>();

    AnimationTimer a = new AnimationTimer() {
        long prev = 0;

        /**
         * dies ist die Mainloop.
         * wird x mal pro Sekunde aufgerufen
         */
        @Override
        public void handle(long l) {
            // 0.01666 60FPS
            double elapsedTime = ((l - prev) / 1000000000.0);
            if (elapsedTime > 0.05) {
                elapsedTime = 0.016666;
            }
            prev = l;
            //Verarbeitung auf der Logikseite fuer die vergangene Zeit seit dem letzten Aufruf
            logic.tickUpdate(elapsedTime);
            //dynamische Objekte wie die Aufzuege veraendern. Farbe und Position
            if (logic.getCommandState().init_done) {
                changeDynamicObjects();
            }
        }
    };

    /**
     * Aufzugshoehe + Uhrzeit aendern
     */
    public void changeDynamicObjects() {
        for (int i = 0; i < logic.getGrid().getElevators().length; i++) {
            elevators.get(i).setTranslateY(logic.getGrid().getElevators()[i].getElevation() * -1);
            passengerElevator.get(i).setText(logic.getGrid().getElevators()[i].getPassengers().size() + "");
        }
        time.setText(logic.getSTime().currentTime());

        for (int i = 0; i < logic.getGrid().getFloors().length; i++) {
            passengerFloor.get(i).setText(logic.getGrid().getFloors()[i].getPassengers().size() + "");
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

    public void changeDoorOpen(int elevatorID) {
        elevators.get(elevatorID).getChildren().get(ELEVATORDOOR_GROUP_NUMBER).setVisible(logic.getGrid().getElevators()[elevatorID].isDoorOpen());
    }


    private void changeElevatorError(int elevatorID) {
        elevators.get(elevatorID).getChildren().get(ELEVATORERROR_GROUP_NUMBER).setVisible(logic.getGrid().getElevators()[elevatorID].isEncounteredError());
    }

    public void displayError(String message, int elevatorID) {
        changeElevatorError(elevatorID);
        // errorMessage.setText(message);
        errorMessage.appendText(message + '\n');
    }

    public void displayErrorMessage(String message) {
        errorMessage.appendText(message);
    }

    public void hideError(int elevatorID) {
        changeElevatorError(elevatorID);
        errorMessage.setText("");
    }

    public void hideErrorMessage() {
        errorMessage.setText("");
    }

    @FXML
    public void changeElevatorErrorTest() {
        for (int i = 0; i < logic.getGrid().elevators.length; i++) {
            logic.getGrid().elevators[i].setEncounteredError(false);
            hideError(i);
        }

    }

    public void showFloorList() {
        passengerFrom.getItems().clear();
        passengerTo.getItems().clear();
        passengerFrom.getItems().addAll(logic.getGrid().floors);
        passengerTo.getItems().addAll(logic.getGrid().floors);
    }

    /**
     * Das Grid zuruecksetzen
     */
    public void clearGrid() {
        ePaneTest.setContent(null);
        elevators.clear();
        floors.clear();
        elevatorbuttons.clear();
        floorbuttons.clear();
        passengerFloor.clear();
        passengerElevator.clear();
        gridAll = new Group();
    }

    /**
     * alle noetige grafische Objekte fuer das Grid erstellen
     */
    public void drawGrid() {
        for (int i = 0; i < logic.getGrid().floors.length; i++) {
            addFloor(i);
        }
        for (int i = 0; i < logic.getGrid().elevators.length; i++) {
            addStaticElevator(i);
        }
        for (int i = 0; i < logic.getGrid().elevators.length; i++) {
            addElevator(i);
        }
    }


    @FXML
    protected void toggleLoop() {
        setLoopRunning(!running);
    }

    /**
     * Die Hauptschleife starten oder stoppen
     *
     * @param run start oder stopp die Schleife
     */
    public void setLoopRunning(boolean run) {
        if (!running && run) {
            a.start();
        } else if (running && !run) {
            a.stop();
        }
        if (this.logic.getGrid() != null)
            this.logic.getSTime().setTimerRunning(run, clockSpeed.getSelectionModel().getSelectedItem());
        running = run;
        if (running)
            pauseButton.setText("pausieren");
        else
            pauseButton.setText("fortsetzen");
    }

    /**
     * zu dem ausgewaehlten SerialPort eine Verbindung aufbauen
     */
    @FXML
    protected void setSelectedSerialPort() {
        if (!logic.isConnected) {
            if (this.serialPane.getSelectionModel().getSelectedItem() != null) {
                this.setLoopRunning(true);
                this.logic.setSerialPort(this.serialPane.getSelectionModel().getSelectedItem());
                System.out.println("connected to: " + this.logic.getSerialPort().getDescriptivePortName());
                serialPane.setMouseTransparent(true);
                this.logic.initConnection();
                connectButton.setText("trennen");
            } else {
                System.out.println("no serial port selected");
            }
        } else {
            logic.closeConnection();
            serialPane.setMouseTransparent(false);
            connectButton.setText("verbinden");
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
        inner.setX(ELEVATOR_WALL_THICKNESS);
        inner.setY(ELEVATOR_WALL_THICKNESS);
        inner.setVisible(false);
        Rectangle errorBorder = new Rectangle();
        errorBorder.setFill(Color.RED);
        errorBorder.setHeight(ELEVATOR_HEIGHT + ELEVATOR_WALL_THICKNESS * 2);
        errorBorder.setWidth(ELEVATOR_WIDTH + ELEVATOR_WALL_THICKNESS * 2);
        errorBorder.setX(-ELEVATOR_WALL_THICKNESS);
        errorBorder.setY(-ELEVATOR_WALL_THICKNESS);
        errorBorder.setVisible(false);
        Group testMovable = new Group();


        testMovable.getChildren().addAll(errorBorder, outer, inner);
        int a = (logic.getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        if (logic.getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTONS_PER_COLUMN_IN_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;
        testMovable.setTranslateX((ELEVATOR_WIDTH) * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum) + ELEVATOR_OFFSET + elevatorNum * ELEVATOR_SPACE_BETWEEN);
        elevators.add(testMovable);
        return testMovable;
    }

    private Group createStaticElevator(int elevatorNum) {
        Group staticElevator = new Group();
        Rectangle shaft = new Rectangle();
        shaft.setFill(Color.LIGHTGRAY);

        shaft.setHeight((logic.getGrid().floors.length) * ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR);
        shaft.setHeight((logic.getGrid().floors[logic.getGrid().floors.length - 1].getHeight() + ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR));
        shaft.setWidth(LINE_THICKNESS);

        int a = (logic.getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        if (logic.getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
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
        shaft.setY((logic.getGrid().floors.length - 1) * ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR * -1);

        staticElevator.getChildren().addAll(passengerAmount, shaft);
        //create FloorButtons
        List<Rectangle> buttonsInElevator = new ArrayList<>();
        for (int j = 0; j < logic.getGrid().elevators[elevatorNum].getButtons().size(); j++) {
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

                    //System.out.println("REQUEST " + elevatorNum + " " + floornum);
                    logic.getOut().add(logic.getGrid().elevators[elevatorNum].getButtons().get(floornum).getOnClick());

                    //FOR MANUAL TESTING (delete later)
                    if (logic.getGrid().elevators[elevatorNum].getMovementDirection() == ElevatorMovement.STAND_STILL) {
                        logic.in.add("MOVE_UP " + elevatorNum);
                    } else if (logic.getGrid().elevators[elevatorNum].getMovementDirection() == ElevatorMovement.UP) {
                        logic.in.add("MOVE_DOWN " + elevatorNum);
                    } else if (logic.getGrid().elevators[elevatorNum].getMovementDirection() == ElevatorMovement.DOWN) {
                        logic.in.add("STOP " + elevatorNum);
                    }

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
        for (int i = 0; i < logic.getGrid().floors[floornum].getButtons().size(); i++) {
            Rectangle outer = new Rectangle();
            outer.setFill(Color.BLACK);
            outer.setHeight(BUTTON_WIDTH);
            outer.setWidth(BUTTON_WIDTH);
            Rectangle inner = new Rectangle();
            inner.setFill(Color.WHITE);
            inner.setHeight(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            inner.setWidth(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            StackPane test = new StackPane();
            test.setTranslateY((logic.getGrid().floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
            test.setTranslateX(i * (BUTTON_WIDTH));
            Text text = new Text("" + logic.getGrid().getFloors()[floornum].getButtons().get(i).getSymbol());
            test.getChildren().addAll(outer, inner, text);
            int fI = i;
            test.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    //System.out.println("BUTTON_PUSH " + floornum + " " + fI);
                    logic.getOut().add(logic.getGrid().floors[floornum].getButtons().get(fI).getOnClick());
                }
            });
            Label label = new Label("0");
            label.setTranslateX(-20);
            label.setTranslateY((logic.getGrid().floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
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
        int a = logic.getGrid().floors.length / BUTTONS_PER_COLUMN_IN_ELEVATOR;
        if (logic.getGrid().floors.length % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;
        int elevatorNum = logic.getGrid().elevators.length;
        heightLine.setWidth(((ELEVATOR_WIDTH * (double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * 1) + ELEVATOR_SPACE_BETWEEN) * elevatorNum - ELEVATOR_SPACE_BETWEEN);
        heightLine.setHeight(LINE_THICKNESS);
        heightLine.setY((logic.getGrid().floors[floorNum].getHeight()) * -1 + ELEVATOR_HEIGHT);
        heightLine.setX(ELEVATOR_OFFSET);
        return heightLine;
    }

    @FXML
    protected void toggleTimerButton() {
        if (logic.getCommandState().init_done) {
            logic.getSTime().setTimerRunning(!logic.getSTime().isTimer_isrunning(), clockSpeed.getSelectionModel().getSelectedItem());
            if (logic.getSTime().isTimer_isrunning()) {
                timerButton.setText("stopp");
            } else {
                timerButton.setText("start");
            }
        }
    }


    @FXML
    protected void showSerialPorts() {
        this.serialPane.getItems().clear();
        this.serialPane.getItems().addAll(SerialPort.getCommPorts());

    }

    @FXML
    protected void addPassenger() {
        if (logic.getCommandState().init_done && this.passengerFrom.getSelectionModel().getSelectedItem() != null && this.passengerTo.getSelectionModel().getSelectedItem() != null)
            logic.addPassenger(passengerFrom.getSelectionModel().getSelectedItem(),
                    passengerTo.getSelectionModel().getSelectedItem());

    }

    @FXML
    protected void setTime() {
        if (logic.getCommandState().init_done && !clockHourField.getText().equals("") && !clockMinuteField.getText().equals(""))
            logic.getSTime().setCurrentTime(clockHourField.getText(), clockMinuteField.getText());

    }

    protected void onClose() {
        this.logic.closeConnection();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //TODO remove
        testErrorHide.setDisable(true);
        //errorMessage Werte setzen
        errorMessage.setEditable(false);
        errorMessage.setStyle("-fx-text-fill: RED;");
        showSerialPorts();

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
        //Fahrgast Herkunftsdarstellung zu einer Zahl aendern
        this.passengerFrom.setCellFactory(e -> new ListCell<Floor>() {
            @Override
            protected void updateItem(Floor f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText("");
                } else {
                    setText(f.getId() + "");
                }
            }
        });
        //Fahrgast Zielsdarstellung zu einer Zahl aendern
        this.passengerTo.setCellFactory(e -> new ListCell<Floor>() {
            @Override
            protected void updateItem(Floor f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText("");
                } else {
                    setText(f.getId() + "");
                }
            }
        });
        //Uhrzeit vorauswaehlen und alle moeglichen Geschwindigkeiten anzeigen
        this.clockSpeed.getItems().addAll(logic.getClockSpeeds());
        this.clockSpeed.getSelectionModel().select(3);
        this.clockSpeed.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (logic.getCommandState().init_done) {
                    logic.getSTime().setTimerRunning(logic.getSTime().isTimer_isrunning(), clockSpeed.getSelectionModel().getSelectedItem());
                }
            }
        });
        this.clockSpeed.setCellFactory(e -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer i, boolean empty) {
                super.updateItem(i, empty);
                if (empty || i == null) {
                    setText("");
                } else {
                    setText(i + " ms");
                }
            }
        });

        //Nur Zahlen sollen moeglich sein im clockHourField
        clockHourField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    clockHourField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        //Nur Zahlen sollen moeglich sein im clockMinuteField
        clockMinuteField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    clockMinuteField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

    }

    @FXML
    protected void testGrid() {
        logic.getCommandState().parse("init_base 3 5 12 5 6");
        logic.getCommandState().parse("init_done");
        logic.getCommandState().parse("light ON e 1 0");
        logic.getCommandState().parse("open 2");
    }
}