package model.dataobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import play.Logger;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

public final class UserDetail {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String mobileNumber;

    private String email;

    private String password;

    private String localeId;

    private String avatarID;

    private String pkid;

    private BigDecimal cash;

    private Integer level;

    private Timestamp joinDate;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocaleId() {
        return localeId;
    }

    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    public String getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(String avatarID) {
        this.avatarID = avatarID;
    }

    public String getPkid() {
        return pkid;
    }

    public void setPkid(String pkid) {
        this.pkid = pkid;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, this);
            return sw.toString();
        } catch (Exception exception) {
            return super.toString();
        }
    }

    public String toKVJSON() {
        List<KeyValue> keyValueList = new ArrayList<KeyValue>();
        keyValueList.add(new KeyValue("first_name", firstName));
        keyValueList.add(new KeyValue("last_name", lastName));
        keyValueList.add(new KeyValue("phone", phoneNumber));
        keyValueList.add(new KeyValue("mobile", mobileNumber));
        keyValueList.add(new KeyValue("email", email));
        keyValueList.add(new KeyValue("password", password));
        keyValueList.add(new KeyValue("locale", localeId));
        keyValueList.add(new KeyValue("avatarId", avatarID));
        keyValueList.add(new KeyValue("cash", cash.toString()));
        keyValueList.add(new KeyValue("level", level.toString()));
        keyValueList.add(new KeyValue("joinDate", joinDate.toString()));

        ObjectMapper mapper = new ObjectMapper();
        try {
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, keyValueList);
            return sw.toString();
        } catch (Exception exception) {
            return super.toString();
        }
    }

    public static final UserDetail buildUserDetailFromJSON(String json) throws Exception {
        Map<String, String> keyValues = KeyValue.buildKeyValuesAsMap(json);

        UserDetail userDetail = new UserDetail();
        userDetail.setFirstName(keyValues.get("first_name"));
        userDetail.setLastName(keyValues.get("last_name"));
        userDetail.setPhoneNumber(keyValues.get("phone"));
        userDetail.setMobileNumber(keyValues.get("mobile"));
        userDetail.setEmail(keyValues.get("email"));
        userDetail.setPassword(keyValues.get("password"));
        userDetail.setLocaleId(keyValues.get("locale"));
        userDetail.setAvatarID(keyValues.get("avatarId"));
        try {
            userDetail.setCash(new BigDecimal(keyValues.get("cash")));
        } catch (Exception exception) {

        }
        try {
            userDetail.setLevel(new Integer(keyValues.get("cash")));
        } catch (Exception exception) {
            userDetail.setLevel(1);
        }
        try {
            userDetail.setJoinDate(new Timestamp(DatatypeConverter.parseDateTime(keyValues.get("joinDate")).getTimeInMillis()));
        } catch (Exception exception) {
            userDetail.setJoinDate(new Timestamp(System.currentTimeMillis()));
        }
        //TODO validate password 2
        keyValues.get("password2");
        return userDetail;
    }

    public static final UserDetail mergeForUpdate(UserDetail newUserDetail, UserDetail oldUserDetail) throws Exception {
        UserDetail mergedUserDetail = new UserDetail();
        mergedUserDetail.setAvatarID(getDbFriendly(isEmpty(newUserDetail.getAvatarID()) ? oldUserDetail.getAvatarID() : newUserDetail.getAvatarID()));
        mergedUserDetail.setEmail(getDbFriendly(isEmpty(newUserDetail.getEmail()) ? oldUserDetail.getEmail() : newUserDetail.getEmail()));
        mergedUserDetail.setFirstName(getDbFriendly(isEmpty(newUserDetail.getFirstName()) ? oldUserDetail.getFirstName() : newUserDetail.getFirstName()));
        mergedUserDetail.setLastName(getDbFriendly(isEmpty(newUserDetail.getLastName()) ? oldUserDetail.getLastName() : newUserDetail.getLastName()));
        mergedUserDetail.setLocaleId(getDbFriendly(isEmpty(newUserDetail.getLocaleId()) ? oldUserDetail.getLocaleId() : newUserDetail.getLocaleId()));
        mergedUserDetail.setMobileNumber(getDbFriendly(isEmpty(newUserDetail.getMobileNumber()) ? oldUserDetail.getMobileNumber() : newUserDetail.getMobileNumber()));
        mergedUserDetail.setPassword(getDbFriendly(isEmpty(newUserDetail.getPassword()) ? oldUserDetail.getPassword() : newUserDetail.getPassword()));
        mergedUserDetail.setPhoneNumber(getDbFriendly(isEmpty(newUserDetail.getPhoneNumber()) ? oldUserDetail.getPhoneNumber() : newUserDetail.getPhoneNumber()));
        mergedUserDetail.setPkid(oldUserDetail.getPkid());
        mergedUserDetail.setCash(isBDEmpty(newUserDetail.getCash()) ? oldUserDetail.getCash() : newUserDetail.getCash());
        mergedUserDetail.setLevel(isIntEmpty(newUserDetail.getLevel()) ? oldUserDetail.getLevel() : newUserDetail.getLevel());
        mergedUserDetail.setJoinDate(oldUserDetail.getJoinDate());
        return mergedUserDetail;
    }

    private static boolean isEmpty(String someString) {
        if (someString == null || someString.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isBDEmpty(BigDecimal someNumber) {
        if (someNumber == null || someNumber.signum() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIntEmpty(Integer someNumber) {
        if (someNumber == null || someNumber == 0) {
            return true;
        } else {
            return false;
        }
    }

    private static String getDbFriendly(String someString) {
        if (someString == null) {
            return "";
        } else {
            return someString;
        }
    }
}
