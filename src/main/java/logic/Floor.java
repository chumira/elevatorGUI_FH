package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Floor {

    int id;
    List<Passenger> passengers = new ArrayList<>();
    List<LEDButton> buttons = new ArrayList<>();
    double height;

    public Floor(int id) {
        this.id = id;
    }
}
