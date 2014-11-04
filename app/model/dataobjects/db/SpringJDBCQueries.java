package model.dataobjects.db;

import model.dataobjects.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class SpringJDBCQueries {

    private JdbcTemplate jdbcTemplate;

    public SpringJDBCQueries(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<UserStock> getUserStocks(String userId) {
        List<UserStock> userStocks = this.jdbcTemplate.query(
                "select SYMBOL, SUM(UNITS) as VALUE " +
                        "from MIURA.USERPORTFOLIO US1 " +
                        "group by USERID, SYMBOL " +
                        "having USERID = ? AND SUM(UNITS) > 0",
                new Object[]{userId},
                new UserStockMapper());
        return userStocks;
    }

    public List<UserStock> getAllStocks() {
        List<UserStock> userStocks = this.jdbcTemplate.query(
                "select distinct(SYMBOL) from MIURA.NASDAQUPDATE",
                new Object[]{},
                new UserStockMapper());
        return userStocks;
    }

    public List<UserStock> getStockOpen(String symbol) {
        List<UserStock> userStocks = this.jdbcTemplate.query(
                "select SYMBOL, LASTSALE as VALUE " +
                        "from MIURA.NASDAQUPDATE " +
                        "where SYMBOL = ?",
                new Object[]{symbol},
                new UserStockMapper());
        return userStocks;
    }

    private static final class UserStockMapper implements RowMapper<UserStock> {
        public UserStock mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserStock userStock = new UserStock(rs.getString("SYMBOL"), rs.getFloat("VALUE"));
            return userStock;
        }
    }

    public List<Trade> getUserTradeHistory(String userId, String symbol, boolean desc) {
        List<Trade> trades = this.jdbcTemplate.query(
                "select TIMEPK, QUOTEID, SYMBOL, UNITS, PRICE, BUYSELL " +
                        "from MIURA.USERPORTFOLIO " +
                        "where USERID = ? AND SYMBOL = ? " +
                        "order by TIMEPK " + (desc ? "desc" : "asc"),
                new Object[]{userId},
                new TradeMapper());
        return trades;
    }

    public List<Trade> getUserTradeHistory(String userId) {
        List<Trade> trades = this.jdbcTemplate.query(
                "select TIMEPK, QUOTEID, SYMBOL, UNITS, PRICE, BUYSELL, USERID " +
                        "from MIURA.USERPORTFOLIO " +
                        "where USERID = ? " +
                        "order by TIMEPK desc",
                new Object[]{userId},
                new TradeMapper());
        return trades;
    }

    private static final class TradeMapper implements RowMapper<Trade> {
        public Trade mapRow(ResultSet rs, int rowNum) throws SQLException {
            Trade trade = new Trade(new Timestamp(rs.getLong("TIMEPK")), rs.getString("USERID"), rs.getString("SYMBOL"), rs.getFloat("UNITS"), rs.getFloat("PRICE"), rs.getString("BUYSELL"));
            return trade;
        }
    }

    public EnvProps getEnvProps(String key) {
        return this.jdbcTemplate.query(
                "select PROPKEY, PROPVALUE from MIURA.MIURAENV where PROPKEY = ?",
                new Object[]{key},
                new EnvPropMapper()).get(0);
    }

    private static final class EnvPropMapper implements RowMapper<EnvProps> {
        public EnvProps mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvProps envProps = new EnvProps(rs.getString("PROPKEY"), rs.getString("PROPVALUE"));
            return envProps;
        }
    }

    public UserDetail selectUserByEmail(String userEmail) {
        List<UserDetail> userDetails = this.jdbcTemplate.query(
                "select USERIDPK, USERPASSWORD, FIRSTNAME ,LASTNAME, EMAIL, TWITTERHANDLE, FACEBOOKHANDLE, " +
                        "GOOGLEHANDLE, LINKEDINHANDLE, LOCALEID, AVATARID, CASH " +
                        "from MIURA.GAMEUSER where EMAIL = ?",
                new Object[]{userEmail},
                new UserDetailMapper());
        return userDetails.size() == 1 ? userDetails.get(0) : null;
    }

    private static final class UserDetailMapper implements RowMapper<UserDetail> {
        public UserDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDetail userDetail = new UserDetail();
            userDetail.setPkid(rs.getString("USERIDPK"));
            userDetail.setPassword(rs.getString("USERPASSWORD"));
            userDetail.setAvatarID(rs.getString("AVATARID"));
            userDetail.setEmail(rs.getString("EMAIL"));
            userDetail.setFirstName(rs.getString("FIRSTNAME"));
            userDetail.setLastName(rs.getString("LASTNAME"));
            userDetail.setLocaleId(rs.getString("LOCALEID"));
            userDetail.setCash(rs.getBigDecimal("CASH"));
            return userDetail;
        }
    }

    public NASDAQStockInfo getStockDailyInfo(String symbol) {
        List<NASDAQStockInfo> stockInfo = this.jdbcTemplate.query(
                "select TIMEPK, SYMBOL, SECTOR ,MARKETCAP, LASTSALE, INDUSTRY " +
                        "from MIURA.NASDAQUPDATE where SYMBOL = ? ORDER BY TIMEPK DESC",
                new Object[]{symbol},
                new NASDAQStockInfoMapper());
        return stockInfo.size() > 0 ? stockInfo.get(0) : null;
    }

    private static final class NASDAQStockInfoMapper implements RowMapper<NASDAQStockInfo> {
        public NASDAQStockInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            NASDAQStockInfo stockInfo = new NASDAQStockInfo();
            stockInfo.setSymbol(rs.getString("TIMEPK"));
            stockInfo.setLastSaleRate(rs.getDouble("LASTSALE"));
            stockInfo.setMarketCap(rs.getDouble("MARKETCAP"));
            stockInfo.setIndustry(rs.getString("INDUSTRY"));
            stockInfo.setSector(rs.getString("SECTOR"));
            return stockInfo;
        }
    }

}
