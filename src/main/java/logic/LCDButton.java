package logic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LCDButton {
    int id;
    String symbol;
    String onClick;
    boolean isGlowing = false;


    public LCDButton(String symbol, String onClick) {
        this.symbol = symbol;
        this.onClick = onClick;

    }
}
