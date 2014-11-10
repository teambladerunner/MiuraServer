package model.dataobjects;

import java.sql.Timestamp;
import java.util.Map;

public class Trade {

    private final String symbol;

    private final Float units;

    private final Float rate;

    private final Timestamp time;

    private final String buySell;

    private final String user;

    private final String pending;

    public Trade(Timestamp time, String user, String symbol, Float units, Float rate, String buySell, String pending) {
        this.time = time;
        this.symbol = symbol;
        this.units = units;
        this.rate = rate;
        this.buySell = buySell;
        this.user = user;
        this.pending = pending;
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

    public String getUser() {
        return user;
    }

    public String getPending() {
        return pending;
    }

    public static final Trade buildFromJSON(String user, String json) throws Exception {
        Map<String, String> keyValues = KeyValue.buildKeyValuesAsMap(json);
        return new Trade(new Timestamp(System.currentTimeMillis()), user, keyValues.get("symbol"), Float.parseFloat(keyValues.get("units")), Float.parseFloat(keyValues.get("rate")), keyValues.get("buy_sell"), "Y");
    }
}
