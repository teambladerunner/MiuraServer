package model.dataobjects;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;
import java.util.Map;

public final class UserDetail {

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String mobileNumber;

    private String email;

    private String password;

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

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, this);
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
        //TODO validate password 2
        keyValues.get("password2");
        return userDetail;
    }
}
