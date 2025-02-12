package logic;

import lombok.Getter;
import lombok.Setter;

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
