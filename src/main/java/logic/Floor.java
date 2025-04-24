package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Floor {

    int id;
    List<Passenger> passengers = new ArrayList<>();
    LEDButton callButton;
    List<LEDButton> updownButtons;
    LEDButton emergency;
    double height;

    public Floor(int id) {
        this.id = id;
    }

    public List<LEDButton> aggregateButtons() {
        List<LEDButton> res = new LinkedList<>();
        if (callButton != null)
            res.add(callButton);
        if (updownButtons != null && updownButtons.size() > 0)
            res.addAll(updownButtons);
        if (emergency != null)
            res.add(emergency);
        return res;
    }
}
