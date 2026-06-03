package db.model;

import db.index.TableIndex;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private final TableSchema schema;
    private final List<Row> rows = new ArrayList<>();
    private final TableIndex index;

    public Table(TableSchema schema) {
        this.schema = schema;
        this.index = new TableIndex(schema.columnNames());
    }

    public TableSchema schema() {
        return schema;
    }

    public List<Row> rows() {
        return new ArrayList<>(rows);
    }

    public void insert(Row row) {
        rows.add(row);
        index.add(row);
    }

    public void update(Row row, String column, Value value) {
        index.remove(row);
        row.set(column, value);
        index.add(row);
    }

    public void delete(Row row) {
        rows.remove(row);
        index.remove(row);
    }

    public SearchResult search(Condition condition) {
        Column column = schema.column(condition.column());
        Value wanted = Value.fromLiteral(condition.literal(), column.type());

        if (condition.operator() == Operator.EQ) {
            List<Row> matches = index.find(condition.column(), wanted);
            return new SearchResult(matches, true);
        }

        List<Row> matches = new ArrayList<>();
        for (Row row : rows) {
            if (matches(row.get(condition.column()), condition.operator(), wanted)) {
                matches.add(row);
            }
        }
        return new SearchResult(matches, false);
    }

    private boolean matches(Value actual, Operator operator, Value expected) {
        int comparison = actual.compareTo(expected);
        return switch (operator) {
            case EQ -> comparison == 0;
            case NE -> comparison != 0;
            case GT -> comparison > 0;
            case LT -> comparison < 0;
            case GTE -> comparison >= 0;
            case LTE -> comparison <= 0;
        };
    }
}
