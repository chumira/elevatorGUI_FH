package logic;

import gui.GuiController;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;

import java.util.ResourceBundle;

/**
 * leere Klasse als alternative fuer GuiController zum testen
 */
public class GuiControllerEmpty extends GuiController {
    public void changeDynamicObjects() {

    }

    public void changeElevatorButtonLight(boolean on, int elevatorID, int buttonID) {

    }

    public void changeFloorButtonLight(boolean on, int floorID, int buttonID) {

    }

    public void changeDoorOpen(int elevatorID) {
    }


    private void changeElevatorError(int elevatorID) {
    }

    public void displayError(String message, int elevatorID) {
    }

    public void displayErrorMessage(String message) {
    }

    public void hideError(int elevatorID) {
    }

    public void hideErrorMessage() {
    }


    public void changeElevatorErrorTest() {


    }

    public void showFloorList() {

    }


    public void clearGrid() {

    }

    public void drawGrid() {

    }


    protected void toggleLoop() {

    }


    public void setLoopRunning(boolean run) {

    }


    protected void setSelectedSerialPort() {

    }


    protected void addElevator(int elevatorNum) {

    }


    protected void addFloor(int floornum) {

    }


    protected void addStaticElevator(int elevatorNum) {

    }

    protected void addDestinationMarkers() {

    }


    private Group createElevator(int elevatorNum) {
        assert (false);
        return new Group();
    }


    private Group createStaticElevator(int elevatorNum) {
        assert (false);
        return new Group();
    }


    private Group createFloor(int floornum) {
        assert (false);
        return new Group();
    }


    private Rectangle createFloorLine(int floorNum) {
        assert (false);
        return new Rectangle();
    }


    private Circle[][] createPassengerDestinations() {
        assert (false);
        return new Circle[0][0];
    }


    protected void toggleTimerButton() {


    }


    protected void showSerialPorts() {

    }


    protected void addPassenger() {

    }


    protected void setTime() {

    }


    protected void onClose() {

    }

    public void showDestination(boolean show, int elevatorNum, int floorNum) {

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    protected void testGrid() {


    }


    private double calcElevatorPos(int elevatorNum) {
        assert (false);
        return 0;
    }


    private void setPosForElevatorButtons(StackPane test2, int elevatorNum, int buttonNum) {
    }

}
