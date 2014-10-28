package model.dataobjects;

public class UserStock {

    private final String symbol;

    private final Float units;

    public UserStock(String symbol, Float units) {
        this.symbol = symbol;
        this.units = units;
    }

    public String getSymbol() {
        return symbol;
    }

    public Float getUnits() {
        return units;
    }
}
