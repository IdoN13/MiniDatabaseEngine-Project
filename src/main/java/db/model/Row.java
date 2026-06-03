package db.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Row {
    private final Map<String, Value> values = new LinkedHashMap<>();

    public void set(String column, Value value) {
        values.put(column, value);
    }

    public Value get(String column) {
        Value value = values.get(column);
        if (value == null) {
            throw new IllegalArgumentException("unknown column: " + column);
        }
        return value;
    }

    public Map<String, Value> values() {
        return values;
    }
}
