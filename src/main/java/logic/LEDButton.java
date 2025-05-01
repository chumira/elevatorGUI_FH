/*
 * Author:  Jonas Harmuth
 */
package logic;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation eines Buttons,
 * kann leuchten und hat einen hinterlegten Befehl
 */
@Getter
@Setter
public class LEDButton {
    int id;
    String symbol;
    String onClick;
    boolean isGlowing = false;


    public LEDButton(String symbol, String onClick) {
        this.symbol = symbol;
        this.onClick = onClick;

    }
}
