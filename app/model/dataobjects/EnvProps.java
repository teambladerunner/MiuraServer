package model.dataobjects;

public class EnvProps {

    private final String key;

    private final String value;

    public EnvProps(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
