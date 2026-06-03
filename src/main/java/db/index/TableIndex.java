package db.index;

import db.model.Row;
import db.model.Value;

import java.util.*;

public class TableIndex {
    private final Map<String, Map<Value, List<Row>>> indexes = new HashMap<>();

    public TableIndex(List<String> columns) {
        for (String column : columns) {
            indexes.put(column, new HashMap<>());
        }
    }

    public void add(Row row) {
        for (String column : indexes.keySet()) {
            indexes.get(column).computeIfAbsent(row.get(column), ignored -> new ArrayList<>()).add(row);
        }
    }

    public void remove(Row row) {
        for (String column : indexes.keySet()) {
            List<Row> rows = indexes.get(column).get(row.get(column));
            if (rows != null) {
                rows.remove(row);
            }
        }
    }

    public List<Row> find(String column, Value value) {
        Map<Value, List<Row>> index = indexes.get(column);
        if (index == null) {
            return List.of();
        }
        return new ArrayList<>(index.getOrDefault(value, List.of()));
    }
}
