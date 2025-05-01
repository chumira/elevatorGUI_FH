/*
 * Author:  Jonas Harmuth
 */
package logic;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Representation einer Etage
 */
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
    /**
     * fasst alle Buttons der Etage zu einer Liste zusammen
     * @return Liste mit allen Buttons
     */
    public List<LEDButton> aggregateButtons() {
        List<LEDButton> res = new LinkedList<>();
        if (callButton != null)
            res.add(callButton);
        if (updownButtons != null && !updownButtons.isEmpty())
            res.addAll(updownButtons);
        if (emergency != null)
            res.add(emergency);
        return res;
    }
}
