package logic;

import lombok.Getter;
import lombok.Setter;

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
