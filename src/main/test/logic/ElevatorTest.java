package logic;

import logic.types.ElevatorMovement;
import org.junit.jupiter.api.Test;

public class ElevatorTest {
    Elevator e = new Elevator(1);

    @Test
    public void testElevatorMoveDown() {
        double preElevation = e.getElevation();
        e.setMovementDirection(ElevatorMovement.DOWN);
        e.updateElevation(10);
        assert (e.getElevation() < preElevation);
    }

    @Test
    public void testElevatorMoveUp() {
        double preElevation = e.getElevation();
        e.setMovementDirection(ElevatorMovement.UP);
        e.updateElevation(10);
        assert (e.getElevation() > preElevation);
    }

    @Test
    public void testElevatorStandstill() {
        double preElevation = e.getElevation();
        e.setMovementDirection(ElevatorMovement.STAND_STILL);
        e.updateElevation(10);
        assert (e.getElevation() == preElevation);
    }
}
