package model.dataobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import play.Logger;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValue {

    private String name;

    private String value;

    public KeyValue(){

    }

    public KeyValue(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static List<KeyValue> buildKeyValueList(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<KeyValue> keyValueList =
                objectMapper.readValue(json, typeFactory.constructCollectionType(List.class, KeyValue.class));
        return keyValueList;
    }

    public static Map<String, String> buildKeyValuesAsMap(String json) throws Exception {
        List<KeyValue> keyValueList = buildKeyValueList(json);
        Map<String, String> keyValueMap = new HashMap<String, String>();
        for (KeyValue keyValue : keyValueList) {
            keyValueMap.put(keyValue.getName(), keyValue.getValue());
        }
        return keyValueMap;
    }

}
