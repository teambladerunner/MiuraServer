package model.dataobjects.db;

import model.dataobjects.EnvProps;
import model.dataobjects.Trade;
import model.dataobjects.UserDetail;
import model.dataobjects.UserStock;
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
                "select USERID, SYMBOL, SUM(UNITS) as VALUE " +
                        "from MIURA.USERPORTFOLIO US1 " +
                        "group by USERID, SYMBOL " +
                        "having USERID = ? AND SUM(UNITS) > 0",
                new Object[]{userId},
                new UserStockMapper());
        return userStocks;
    }

    private static final class UserStockMapper implements RowMapper<UserStock> {
        public UserStock mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserStock userStock = new UserStock(rs.getString("SYMBOL"), rs.getFloat("VALUE"));
            return userStock;
        }
    }

    public List<Trade> getUserTradeHistory(String userId) {
        List<Trade> trades = this.jdbcTemplate.query(
                "select TIMEPK, SYMBOL, UNITS, PRICE, BUYSELL from MIURA.USERPORTFOLIO where USERID = ? order by TIMEPK desc",
                new Object[]{userId},
                new TradeMapper());
        return trades;
    }

    private static final class TradeMapper implements RowMapper<Trade> {
        public Trade mapRow(ResultSet rs, int rowNum) throws SQLException {
            Trade trade = new Trade(new Timestamp(rs.getLong("TIMEPK")), rs.getString("SYMBOL"), rs.getFloat("UNITS"), rs.getFloat("PRICE"), rs.getString("BUYSELL"));
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
                        "GOOGLEHANDLE, LINKEDINHANDLE, LOCALEID, AVATARID " +
                        "from MIURA.GAMEUSER where EMAIL = ?",
                new Object[]{userEmail},
                new UserDetailMapper());
        return userDetails.size() == 1 ? userDetails.get(0) : null;
    }

    private static final class UserDetailMapper implements RowMapper<UserDetail> {
        public UserDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDetail userDetail = new UserDetail();
            userDetail.setPkid(rs.getString("USERIDPK"));
            userDetail.setAvatarID(rs.getString("AVATARID"));
            userDetail.setEmail(rs.getString("EMAIL"));
            userDetail.setFirstName(rs.getString("FIRSTNAME"));
            userDetail.setLastName(rs.getString("LASTNAME"));
            userDetail.setLocaleId(rs.getString("LOCALEID"));
            return userDetail;
        }
    }

}
