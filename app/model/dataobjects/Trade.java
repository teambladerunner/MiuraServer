package model.dataobjects;

import java.sql.Timestamp;

public class Trade {

    private final String symbol;

    private final Float units;

    private final Float rate;

    private final Timestamp time;

    private final String buySell;

    public Trade(Timestamp time, String symbol, Float units, Float rate, String buySell) {
        this.time = time;
        this.symbol = symbol;
        this.units = units;
        this.rate = rate;
        this.buySell = buySell;
    }

    public String getSymbol() {
        return symbol;
    }

    public Float getUnits() {
        return units;
    }

    public Float getRate() {
        return rate;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getBuySell() {
        return buySell;
    }
}
