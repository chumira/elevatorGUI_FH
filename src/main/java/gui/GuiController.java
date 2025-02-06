package gui;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import logic.Elevator;
import logic.Floor;
import logic.Logic;

import java.util.ArrayList;
import java.util.List;

public class GuiController {
    @FXML
    private Label welcomeText;
    @FXML
    private VBox serialPane;
    @FXML
    private Button speed;
    @FXML
    private Button addDyn;
    @FXML
    private ScrollPane ePaneTest;
    @FXML
    private HBox eGrid;
    /**
     * Logik
     */
    private Logic logic = new Logic();
    boolean running = false;
    private int eCount = 0;
    private int fCount = 0;

    private final double SPEED = 0.2;
    private double[] speeds = {0, SPEED, -SPEED};
    private Group allElevators = new Group();

    private Group allElevatorDoors = new Group();
    private Group allFloors = new Group();

    private Group gridAll = new Group();
    private List<Group> elevators = new ArrayList<>();
    private List<Group> elevatorDoors = new ArrayList<>();
    private List<Group> floors = new ArrayList<>();
    AnimationTimer a = new AnimationTimer() {
        long prev = 0;

        @Override
        public void handle(long l) {
            // 0.01666 60FPS
            double xD = ((l - prev) / 1000000000.0);
            if (xD > 1f) {
                xD = 0.016666;
            }
            prev = l;
            //speed.setScaleY(speed.getScaleY() + xD);
            //TODO nach Logic auslagern
            logic.getGrid().getElevators()[0].moveUp();
            logic.getGrid().getElevators()[1].moveUp();
            logic.getGrid().getElevators()[2].moveUp();
            for (Elevator e : logic.getGrid().getElevators()
            ) {
                if (e.isMoving()) {

                    for (Floor f : logic.getGrid().getFloors()
                    ) {
                        //TODO ACHTUNG FLOAT
                        if (e.getElevation() - f.getHeight() <= 0.1) {
                            //TODO SEND ARRIVE AT
                        }
                    }
                }
            }
            //TODO DRAW aulagern
            for (int i = 0; i < elevators.size(); i++) {
                elevators.get(i).setTranslateY(logic.getGrid().getElevators()[i].getElevation());
            }
        }
    };

    @FXML
    protected void testCommport() {
        ePaneTest.setContent(null);
        elevators.clear();
        allElevators = new Group();
        if (!running) {
            logic = new Logic(4, 3, 0, 0, 0);
            for (int i = 0; i < logic.getGrid().elevators.length; i++) {
                addElevator();
            }
            for (int i = 0; i < logic.getGrid().floors.length; i++) {
                addFloor();
            }
            for (int i = 0; i < logic.getGrid().elevators.length; i++) {
                addStaticElevator(i);
            }
        } else {
            /**
             * Kann weg
             */
            ePaneTest.setContent(null);
            elevators.clear();
            floors.clear();
            gridAll = new Group();
        }
        if (!running) {
            a.start();
        } else {
            a.stop();
        }
        running = !running;
    }

    @FXML
    protected void addCom() {
        List<String> a = logic.establishSerialConnection();
        for (String b : a
        ) {
            serialPane.getChildren().add(new Label(b));
        }

    }

    @FXML
    protected void addElevator() {
        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }
        //allElevators.getChildren().add(createElevator());
        gridAll.getChildren().add(createElevator());
        //ePaneTest.setContent(gridAll);
        ePaneTest.setContent(gridAll);
        //System.out.println(allElevators.getChildren().size());

    }

    @FXML
    protected void addFloor() {

        if (eGrid == null) {
            eGrid = new HBox();
        }
        if (ePaneTest == null) {
            ePaneTest = new ScrollPane();
        }

        gridAll.getChildren().add(createFloor());
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

    private Group createElevator() {
        Rectangle outer = new Rectangle();
        outer.setFill(Color.BLACK);
        outer.setHeight(60f);
        outer.setWidth(40f);
        Rectangle inner = new Rectangle();
        inner.setFill(Color.GREY);
        inner.setHeight(50f);
        inner.setWidth(30f);
        //ePaneTest.getChildren().addAll(outer,inner);
        inner.setX((elevators.size() + 1) * 100 + 5);
        inner.setY(25);
        outer.setX((elevators.size() + 1) * 100);
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
        shaft.setX(((elevatorNum + 1) * 100 + 20) * 1);
        shaft.setY((floors.size() * 100 - 50) * -1);

        staticElevator.getChildren().addAll(shaft);

        for (int i = 0; i < logic.getGrid().floors.length; i++) {
            Rectangle shaft2 = new Rectangle();
            shaft2.setFill(Color.RED);
            shaft2.setHeight(10f);
            shaft2.setWidth(10f);
            shaft2.setX(((elevatorNum + 1) * 100 + 40) * 1);
            shaft2.setY((floors.size() * 100 - 50 + i * 15) * -1);
            int floornum = i;
            shaft2.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    System.out.println("pressed in E:" + elevatorNum + " to F:" + floornum);
                    if (logic.grid.elevators[elevatorNum].isMoving()) {
                        logic.grid.elevators[elevatorNum].setSpeed(-1 * logic.grid.elevators[elevatorNum].getSpeed());
                    }
                }
            });
            staticElevator.getChildren().addAll(shaft2);
        }


        //TODO BUTTONS in elevator

        return staticElevator;
    }

    private Group createFloor() {
        Rectangle outer = new Rectangle();
        outer.setFill(Color.BLACK);
        outer.setHeight(20f);
        outer.setWidth(20f);
        Rectangle inner = new Rectangle();
        inner.setFill(Color.WHITE);
        inner.setHeight(16f);
        inner.setWidth(16f);

        inner.setY((floors.size() * 100 - 2) * -1);
        inner.setX(22);
        outer.setY(floors.size() * 100 * -1);
        outer.setX(20);
        Group test = new Group();
        Label text = new Label("" + floors.size());
        text.setTranslateX(26);
        text.setTranslateY((floors.size() * 100 - 2) * -1);

        Rectangle heightLine = new Rectangle();
        heightLine.setFill(Color.LIGHTGRAY);
        heightLine.setWidth(elevators.size() * 100);
        heightLine.setHeight(2f);
        heightLine.setY((floors.size() * 100 - 20) * -1);

        test.getChildren().addAll(heightLine, outer, inner, text);
        floors.add(test);
        return test;
    }

}