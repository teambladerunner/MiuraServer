package model.dataobjects;

public class UserStock {

    private final String symbol;

    private final Float price;

    public UserStock(String symbol, Float price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public Float getPrice() {
        return price;
    }

}

