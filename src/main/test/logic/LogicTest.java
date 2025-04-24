package logic;

import com.fazecast.jSerialComm.SerialPort;
import logic.types.ElevatorMovement;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die Logik, zur Vereinfachung wird hier kein Multithreading betrieben
 */
public class LogicTest {
    //TODO tests hier
    Logic logic = new Logic(new GuiControllerEmpty());

    @Test
    public void testConnection() {
        logic.serialPort = SerialPort.getCommPorts()[0];
        assertFalse(logic.serialPort.isOpen());
        logic.initConnection();
        assertTrue(logic.serialPort.isOpen());
        logic.closeConnection();
        assertNull(logic.serialPort);
        logic.serialPort = SerialPort.getCommPorts()[0];
        assertFalse(logic.serialPort.isOpen());
    }

    @Test
    public void testCreateGrid() {
        logic.grid = new ElevatorGrid(2, 2);
        assertEquals(2, logic.grid.floors.length);
        assertEquals(2, logic.grid.elevators.length);
    }


    @Test
    public void testInitNotDone() {
        logic.grid = new ElevatorGrid(2, 2);
        logic.grid.elevators[0].setMovementDirection(ElevatorMovement.UP);
        double prevElevation = logic.grid.elevators[0].getElevation();
        logic.tickUpdate(1000);
        assertEquals(logic.grid.elevators[0].getElevation(), prevElevation);
    }

    @Test
    public void testInitDone() {
        logic.grid = new ElevatorGrid(2, 2);
        logic.grid.elevators[0].setMovementDirection(ElevatorMovement.UP);
        double prevElevation = logic.grid.elevators[0].getElevation();
        logic.getCommandState().init_done = true;
        logic.tickUpdate(1000);
        assertTrue(logic.grid.elevators[0].getElevation() > prevElevation);
    }

    @Test
    public void testParseINIT_PHASE() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        assertEquals(4, logic.grid.floors.length);
        assertEquals(7, logic.grid.elevators.length);
        assertTrue(logic.getCommandState().init_done);
        assertEquals(6, logic.sTime.currentMinute);
        assertEquals(5, logic.sTime.currentHour);
        assertEquals(12, logic.sTime.hoursPerDay);
    }

    @Test
    public void testParseOpenClose() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        assertFalse(logic.grid.elevators[2].doorOpen);
        logic.getCommandState().parse("open 2");
        assertTrue(logic.grid.elevators[2].doorOpen);
        logic.getCommandState().parse("close 2");
        assertFalse(logic.grid.elevators[2].doorOpen);
    }

    @Test
    public void testParseUpStop() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        double elevation = logic.grid.elevators[2].getElevation();
        assertEquals(ElevatorMovement.STAND_STILL, logic.grid.elevators[2].movementDirection);
        logic.getCommandState().parse("move_up 2");
        logic.tickUpdate(1000);
        assertEquals(ElevatorMovement.UP, logic.grid.elevators[2].movementDirection);
        assertTrue(logic.grid.elevators[2].getElevation() > elevation);
        logic.getCommandState().parse("stop 2");
        assertEquals(ElevatorMovement.STAND_STILL, logic.grid.elevators[2].movementDirection);
    }

    @Test
    public void testParseMoveUp_Down_sameAmount() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        double elevation = logic.grid.elevators[2].getElevation();
        assertEquals(ElevatorMovement.STAND_STILL, logic.grid.elevators[2].movementDirection);
        logic.getCommandState().parse("move_up 2");
        logic.tickUpdate(0.1);
        assertEquals(ElevatorMovement.UP, logic.grid.elevators[2].movementDirection);
        assertTrue(logic.grid.elevators[2].getElevation() > elevation);
        logic.getCommandState().parse("move_down 2");
        logic.tickUpdate(0.1);
        assertEquals(elevation, logic.grid.elevators[2].getElevation());
    }

    @Test
    public void testParseLightElevator() {

        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        assertFalse(logic.grid.elevators[1].getButtons().get(0).isGlowing);
        logic.getCommandState().parse("light ON E 1 0");
        assertTrue(logic.grid.elevators[1].getButtons().get(0).isGlowing);

        assertFalse(logic.grid.elevators[2].getButtons().get(3).isGlowing);
        logic.getCommandState().parse("light ON E 2 3");
        assertTrue(logic.grid.elevators[2].getButtons().get(3).isGlowing);
    }

    @Test
    public void testParseLightFloor() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        logic.getCommandState().parse("init_done");
        assertFalse(logic.grid.floors[1].getButtons().get(0).isGlowing);
        logic.getCommandState().parse("light ON F 1 0");
        assertTrue(logic.grid.floors[1].getButtons().get(0).isGlowing);
    }

    @Test
    public void testSimTime() {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        assertNull(logic.sTime);
        logic.getCommandState().parse("init_done");
        assertNotNull(logic.sTime);
        assertFalse(logic.sTime.isTimer_isrunning());
        this.logic.getSTime().setTimerRunning(true, 1000);
        assertTrue(logic.sTime.isTimer_isrunning());
        this.logic.getSTime().setTimerRunning(false, 1000);
        assertFalse(logic.sTime.isTimer_isrunning());
    }


    /**
     * Einen mindestabstand zwischen ARRIVE und LEAVE garantieren
     * Test dauert mehrere Sekunden
     *
     * @throws InterruptedException
     */
    @Test
    public void testARRIVELEAVE_Intervall() throws InterruptedException {
        logic.getCommandState().parse("init_base 4 7 12 5 6");
        assertNull(logic.sTime);
        logic.getCommandState().parse("init_done");
        logic.getCommandState().parse("move_up 0");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        TimestampedMessageAppender appender = new TimestampedMessageAppender("TestAppender");
        appender.start();
        context.getConfiguration().getLoggerConfig("commands").addAppender(appender, null, null);
        context.updateLoggers();
        logic.initOutputThread();
        boolean error = false;
        //logic ausfuehren bis ein Fehler auftritt
        while (!error) {
            //AnimationsTimer simulieren
            //zeit zwischen aufrufen in Sekunden
            logic.tickUpdate(0.0166);
            for (Elevator e : logic.grid.elevators
            ) {
                error = error || e.encounteredError;
            }
            //1000ms/60 = 17 ms
            Thread.sleep(17);
        }
        logic.stopOutputThread();
        List<TimestampedMessageAppender.LogEntry> entries = appender.getEntries();
        List<TimestampedMessageAppender.LogEntry> entriesLEAVE = entries.stream().filter(e -> {
            return e.message.contains("LEAVE");
        }).toList();
        List<TimestampedMessageAppender.LogEntry> entriesARRIVE = entries.stream().filter(e -> {
            return e.message.contains("ARRIVE");
        }).toList();
        assertEquals(entriesARRIVE.size(), entriesLEAVE.size());
        //delta0 ist kleiner, da der elevator schon auf halber Hoehe ist
        long delta0 = entriesLEAVE.get(0).timestamp - entriesARRIVE.get(0).timestamp;
        long delta1 = entriesLEAVE.get(1).timestamp - entriesARRIVE.get(1).timestamp;
        long delta2 = entriesLEAVE.get(2).timestamp - entriesARRIVE.get(2).timestamp;

        assertTrue(delta0 >= 100, "Der Abstand0 muss mindestens 100ms betragen");
        assertTrue(delta1 >= 200, "Der Abstand1 muss mindestens 200ms betragen");
        assertTrue(delta2 >= 200, "Der Abstand2 muss mindestens 200ms betragen");

    }


}
