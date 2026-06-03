package db.engine;

import java.util.List;

public class QueryResult {
    private final String text;

    private QueryResult(String text) {
        this.text = text;
    }

    public static QueryResult message(String message) {
        return new QueryResult(message);
    }

    public static QueryResult table(List<String> columns, List<List<String>> rows, String searchMode) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(" | ", columns)).append(System.lineSeparator());
        builder.append("-".repeat(Math.max(3, builder.length() - 1))).append(System.lineSeparator());
        for (List<String> row : rows) {
            builder.append(String.join(" | ", row)).append(System.lineSeparator());
        }
        builder.append(rows.size()).append(" rows, ").append(searchMode);
        return new QueryResult(builder.toString());
    }

    @Override
    public String toString() {
        return text;
    }
}
