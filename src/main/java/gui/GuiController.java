package gui;

import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    private static final double ELEVATOR_HEIGHT = 80;
    private static final double ELEVATOR_WIDTH = 60;
    private static final double ELEVATOR_WALL_THICKNESS = 5;
    private static final double BUTTON_BORDER_THICKNESS = 2;
    private static final double LINE_THICKNESS = 2;
    private static final double ELEVATOR_OFFSET = 20;
    private static final int BUTTONS_PER_COLUMN_IN_ELEVATOR = 2;
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
    private Circle[][] destination;

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
        for (int i = 0; i < logic.grid.getElevators().length; i++) {
            elevators.get(i).setTranslateY(logic.grid.getElevators()[i].getElevation() * -1);
            passengerElevator.get(i).setText(logic.grid.getElevators()[i].getPassengers().size() + "");
        }
        time.setText(logic.getSTime().currentTime());

        for (int i = 0; i < logic.grid.getFloors().length; i++) {
            passengerFloor.get(i).setText(logic.grid.getFloors()[i].getPassengers().size() + "");
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
        elevators.get(elevatorID).getChildren().get(ELEVATORDOOR_GROUP_NUMBER).setVisible(logic.grid.getElevators()[elevatorID].isDoorOpen());
    }


    private void changeElevatorError(int elevatorID) {
        elevators.get(elevatorID).getChildren().get(ELEVATORERROR_GROUP_NUMBER).setVisible(logic.grid.getElevators()[elevatorID].isEncounteredError());
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
        for (int i = 0; i < logic.grid.elevators.length; i++) {
            logic.grid.elevators[i].setEncounteredError(false);
            hideError(i);
        }

    }

    public void showFloorList() {
        passengerFrom.getItems().clear();
        passengerTo.getItems().clear();
        passengerFrom.getItems().addAll(logic.grid.floors);
        passengerTo.getItems().addAll(logic.grid.floors);
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
        for (int i = 0; i < logic.grid.floors.length; i++) {
            addFloor(i);
        }
        for (int i = 0; i < logic.grid.elevators.length; i++) {
            addStaticElevator(i);
        }
        for (int i = 0; i < logic.grid.elevators.length; i++) {
            addElevator(i);
        }
        addDestinationMarkers();
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
        if (this.logic.grid != null)
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
                logic.logInfo("connected to: " + this.logic.getSerialPort().getDescriptivePortName());
                serialPane.setMouseTransparent(true);
                this.logic.initConnection();
                connectButton.setText("trennen");
            } else {
                this.logic.logWarn("no serial port chosen");
            }
        } else {
            logic.logInfo("closed connection with: " + this.logic.getSerialPort().getDescriptivePortName());
            logic.closeConnection();
            clearGrid();
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

    protected void addDestinationMarkers() {
        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }
        destination = createPassengerDestinations();
        ePaneTest.setContent(gridAll);
    }

    /**
     * erzeugt einen Aufzug
     *
     * @param elevatorNum um welchen Aufzug es sich handelt
     * @return eine Gruppe aus Elementen, welche einen Aufzug darstellen
     */
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
        Group elevator = new Group();
        elevator.getChildren().addAll(errorBorder, outer, inner);
        //Berechnung Anzahl benoetigter Spalten
        int a = (logic.grid.elevators[elevatorNum].getButtons().size() / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        //wenn es nicht glatt aufgeht muss es eine extra Spalte geben
        if (logic.grid.elevators[elevatorNum].getButtons().size() % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        //mindestens so viele Spalten wie unter einen Aufzug passen
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;
        //X-Koordinate setzen
        elevator.setTranslateX((ELEVATOR_WIDTH) * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum)
                + ELEVATOR_OFFSET + this.logic.grid.getMaxAmountButtons() * BUTTON_WIDTH
                + elevatorNum * ELEVATOR_SPACE_BETWEEN);
        elevator.setTranslateX(calcSpaceBetweenElevators(elevatorNum) - ELEVATOR_WIDTH / 2);
        elevators.add(elevator);
        return elevator;
    }

    /**
     * erzeugt alle Elemente fuer einen Aufzug die sich nicht mit dem Aufzug mitbewegen wie Knoepfe und eine Mittellinie
     *
     * @param elevatorNum fuer welchen Aufzug
     * @return eine Gruppe aus allen statischen Elementen eines Aufzuges
     */
    private Group createStaticElevator(int elevatorNum) {
        Group staticElevator = new Group();
        Rectangle shaft = new Rectangle();
        shaft.setFill(Color.LIGHTGRAY);
        shaft.setHeight((logic.grid.floors.length) * ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR);
        shaft.setHeight((logic.grid.floors[logic.grid.floors.length - 1].getHeight() + ElevatorGrid.HEIGHT_INCREASE_PER_FLOOR));
        shaft.setWidth(LINE_THICKNESS);

        //siehe createElevator


        Label passengerAmount = new Label();
        passengerAmount.setLayoutY(ELEVATOR_HEIGHT * 1.1 + BUTTONS_PER_COLUMN_IN_ELEVATOR * BUTTON_WIDTH);
        passengerAmount.setStyle("-fx-font: 16 arial;");
        passengerAmount.setText("0");
        passengerAmount.setAlignment(Pos.CENTER);
        passengerAmount.setTranslateX(calcSpaceBetweenElevators(elevatorNum));
        passengerElevator.add(passengerAmount);
        shaft.setX(calcSpaceBetweenElevators(elevatorNum) - LINE_THICKNESS / 2);
        shaft.setY(logic.grid.floors[logic.grid.floors.length - 1].getHeight() * -1);
        staticElevator.getChildren().addAll(passengerAmount, shaft);
        staticElevator.setViewOrder(11);
        //create FloorButtons
        List<Rectangle> buttonsInElevator = new ArrayList<>();
        //jeden Knopf erzeugen
        for (int i = 0; i < logic.grid.elevators[elevatorNum].getButtons().size(); i++) {
            Rectangle floorLCD_Border = new Rectangle();
            floorLCD_Border.setFill(Color.BLACK);
            floorLCD_Border.setHeight(BUTTON_WIDTH);
            floorLCD_Border.setWidth(BUTTON_WIDTH);
            Rectangle floorLCD = new Rectangle();
            floorLCD.setFill(Color.GRAY);
            floorLCD.setHeight(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            floorLCD.setWidth(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            Text test = new Text("" + i);
            test.setStyle("-fx-font: 16 arial;");
            StackPane test2 = new StackPane(floorLCD_Border, floorLCD, test);
            setPosForElevatorButtons(test2, elevatorNum, i);
            test2.setLayoutY((ELEVATOR_HEIGHT * 1.1 + i % BUTTONS_PER_COLUMN_IN_ELEVATOR * BUTTON_WIDTH));
            int floornum = i;
            test2.setOnMouseClicked(mouseEvent ->
                    logic.getOut().add(logic.grid.elevators[elevatorNum].getButtons().get(floornum).getOnClick()));
            staticElevator.getChildren().addAll(test2);
            buttonsInElevator.add(floorLCD);
        }
        elevatorbuttons.add(buttonsInElevator);

        return staticElevator;
    }


    /**
     * erzeugt alles noetige fuer eine Etage
     *
     * @param floornum welche Etage
     * @return eine Gruppe aus Elementen fuer eine Etage
     */
    private Group createFloor(int floornum) {
        Group allButtonsFloor = new Group();
        List<Rectangle> buttonsFloor = new ArrayList<>();
        for (int i = 0; i < logic.grid.floors[floornum].getButtons().size(); i++) {
            Rectangle outer = new Rectangle();
            outer.setFill(Color.BLACK);
            outer.setHeight(BUTTON_WIDTH);
            outer.setWidth(BUTTON_WIDTH);
            Rectangle inner = new Rectangle();
            inner.setFill(Color.WHITE);
            inner.setHeight(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            inner.setWidth(BUTTON_WIDTH - BUTTON_BORDER_THICKNESS);
            StackPane test = new StackPane();
            test.setTranslateY((logic.grid.floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
            test.setTranslateX(i * (BUTTON_WIDTH));
            Text text = new Text("" + logic.grid.getFloors()[floornum].getButtons().get(i).getSymbol());
            test.getChildren().addAll(outer, inner, text);
            int fI = i;
            test.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    //System.out.println("BUTTON_PUSH " + floornum + " " + fI);
                    logic.getOut().add(logic.grid.floors[floornum].getButtons().get(fI).getOnClick());
                }
            });
            Label label = new Label("0");
            label.setTranslateX(-20);
            label.setStyle("-fx-font: 16 arial;");
            label.setTranslateY((logic.grid.floors[floornum].getHeight()) * -1 + ELEVATOR_HEIGHT - BUTTON_WIDTH / 2);
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
        int a = logic.grid.elevators[0].getButtons().size() / BUTTONS_PER_COLUMN_IN_ELEVATOR;
        if (logic.grid.elevators[0].getButtons().size() % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;
        int elevatorNum = logic.grid.elevators.length;
        heightLine.setWidth(((ELEVATOR_WIDTH * (double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * 1) + ELEVATOR_SPACE_BETWEEN) * elevatorNum - ELEVATOR_SPACE_BETWEEN);
        heightLine.setHeight(LINE_THICKNESS);
        heightLine.setY((logic.grid.floors[floorNum].getHeight()) * -1 + ELEVATOR_HEIGHT - LINE_THICKNESS / 2);
        heightLine.setX(ELEVATOR_OFFSET + this.logic.grid.getMaxAmountButtons() * BUTTON_WIDTH);
        heightLine.setViewOrder(11);
        return heightLine;
    }

    private Circle[][] createPassengerDestinations() {
        Circle[][] a = new Circle[this.logic.grid.elevators.length][this.logic.grid.floors.length];
        for (int i = 0; i < this.logic.grid.elevators.length; i++) {
            for (int j = 0; j < this.logic.grid.floors.length; j++) {
                a[i][j] = new Circle();
                a[i][j].setRadius(LINE_THICKNESS * 3);
                a[i][j].setFill(Color.RED);
                a[i][j].setCenterY(((logic.grid.floors[j].getHeight()) * -1 + ELEVATOR_HEIGHT));
                a[i][j].setCenterX(calcSpaceBetweenElevators(i));
                a[i][j].setVisible(false);
                a[i][j].setViewOrder(10);
                gridAll.getChildren().add(a[i][j]);
            }
        }
        return a;
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
        this.logic.closeThreads();
    }

    public void showDestination(boolean show, int elevatorNum, int floorNum) {
        destination[elevatorNum][floorNum].setVisible(show);
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
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        logic.getCommandState().parse("light ON e 1 0");
        logic.getCommandState().parse("open 2");

    }


    private double calcSpaceBetweenElevators(int elevatorNum) {
        double res;

        //Berechnung Anzahl benoetigter Spalten
        int a = (logic.grid.elevators[0].getButtons().size() / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        //wenn es nicht glatt aufgeht muss es eine extra Spalte geben
        if (logic.grid.elevators[0].getButtons().size() % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        //mindestens so viele Spalten wie unter einen Aufzug passen
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;


        res = ((ELEVATOR_WIDTH * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum)
                + ELEVATOR_OFFSET + this.logic.grid.getMaxAmountButtons() * BUTTON_WIDTH + ELEVATOR_WIDTH / 2 + elevatorNum * ELEVATOR_SPACE_BETWEEN) - LINE_THICKNESS / 2);
        return res;
    }

    private void setPosForElevatorButtons(StackPane test2, int elevatorNum, int buttonNum) {
        //Berechnung Anzahl benoetigter Spalten
        int a = (logic.grid.elevators[0].getButtons().size() / BUTTONS_PER_COLUMN_IN_ELEVATOR);
        //wenn es nicht glatt aufgeht muss es eine extra Spalte geben
        if (logic.grid.elevators[0].getButtons().size() % BUTTONS_PER_COLUMN_IN_ELEVATOR != 0)
            a++;
        //mindestens so viele Spalten wie unter einen Aufzug passen
        if (a < BUTTON_COLUMNS_UNDER_ELEVATOR)
            a = BUTTON_COLUMNS_UNDER_ELEVATOR;
        //Berechnug welche Spalte und in welcher Reihe
        int columnLength = buttonNum / BUTTONS_PER_COLUMN_IN_ELEVATOR;
        test2.setLayoutX((ELEVATOR_WIDTH * ((double) a / BUTTON_COLUMNS_UNDER_ELEVATOR * elevatorNum)
                + ELEVATOR_OFFSET + this.logic.grid.getMaxAmountButtons()
                * BUTTON_WIDTH + BUTTON_WIDTH * columnLength) + elevatorNum * ELEVATOR_SPACE_BETWEEN);


    }
}