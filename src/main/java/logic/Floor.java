package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Floor {

    String id;
    List<Passenger> passengers = new ArrayList<>();
    List<LCDButton> buttons = new ArrayList<>();
    double height;
    public Floor(String id){
        this.id = id;
    }
}
