package model.dataobjects;


public class NASDAQStockInfo {
    private String symbol;

    private Double lastSaleRate;

    private Double marketCap;

    private String sector;

    private String industry;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getLastSaleRate() {
        return lastSaleRate;
    }

    public void setLastSaleRate(Double lastSaleRate) {
        this.lastSaleRate = lastSaleRate;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
