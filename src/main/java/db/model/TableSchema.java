package db.model;

import java.util.ArrayList;
import java.util.List;

public record TableSchema(String name, List<Column> columns) {
    public Column column(String name) {
        for (Column column : columns) {
            if (column.name().equals(name)) {
                return column;
            }
        }
        throw new IllegalArgumentException("unknown column: " + name);
    }

    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        for (Column column : columns) {
            names.add(column.name());
        }
        return names;
    }
}
