package model.stocks;

public interface StockQuote {
    public Double newPrice(String symbol, Double lastPrice);
}
