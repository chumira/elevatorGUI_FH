package logic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LCDButton {
    String id;
    String symbol;
    String onClick;
    boolean isGlowing;

    public LCDButton() {
        this.isGlowing = false;
    }

    public LCDButton(String symbol, String onClick) {
        super();
        this.symbol = symbol;
        this.onClick = onClick;

    }
}
