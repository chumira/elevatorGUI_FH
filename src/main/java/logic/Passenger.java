/*
 * Author:  Jonas Harmuth
 */
package logic;

import logic.types.Activity;
import lombok.Getter;
import lombok.Setter;

/**
 * Represenation eines Fahrgastes
 * mit Start- und Zielebene
 */
@Getter
@Setter
public class Passenger {
    String id;
    Floor origin, destination;
    Activity activity = Activity.IS_WAITING;

    Passenger(Floor origin, Floor destination) {
        this.origin = origin;
        this.destination = destination;

    }
}
