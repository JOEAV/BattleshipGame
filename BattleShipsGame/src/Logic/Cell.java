package Logic;

import java.awt.*;

public class Cell {
    private Point where;
    private boolean isGoodHit;
    private String sign;

    public Cell(Point place, String i_Sign) {
        sign = i_Sign;
        where = place;
    }

    public enum BoardSigns {
        hit("V"),
        miss("X"),
        ship("#"),
        mine("M"),
        none(" ");

        private String sign;

        public String sign() {
            return sign;
        }

        BoardSigns(String x) {
            this.sign = x;
        }
    }

    public void setNewSign(String newSign){
        sign = newSign;
    }

    public void setGoodHit(boolean goodHit) {
        isGoodHit = goodHit;
    }

    public Point getWhere() {
        return where;
    }

    public String getSign() {
        return sign;
    }

    public boolean isGoodHit() {
        return isGoodHit;
    }
}
