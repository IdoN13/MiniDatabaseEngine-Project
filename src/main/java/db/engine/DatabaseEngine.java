package db.engine;

import db.model.*;
import db.parser.command.*;
import db.storage.FileStorage;

import java.io.IOException;
import java.util.*;

public class DatabaseEngine {
    private final FileStorage storage;
    private final Map<String, Table> tables = new LinkedHashMap<>();

    public DatabaseEngine(FileStorage storage) {
        this.storage = storage;
        try {
            tables.putAll(storage.loadAll());
        } catch (IOException ex) {
            throw new IllegalStateException("could not load database files: " + ex.getMessage(), ex);
        }
    }

    public QueryResult execute(Command command) {
        if (command instanceof CreateTableCommand c) return createTable(c);
        if (command instanceof InsertCommand c) return insert(c);
        if (command instanceof SelectCommand c) return select(c);
        if (command instanceof UpdateCommand c) return update(c);
        if (command instanceof DeleteCommand c) return delete(c);
        throw new IllegalArgumentException("unsupported command");
    }

    public String listTables() {
        if (tables.isEmpty()) {
            return "no tables";
        }
        return String.join("\n", tables.keySet());
    }

    public String describe(String tableName) {
        Table table = table(tableName);
        StringBuilder text = new StringBuilder();
        for (Column column : table.schema().columns()) {
            text.append(column.name()).append(" ").append(column.type()).append(System.lineSeparator());
        }
        return text.toString().trim();
    }

    private QueryResult createTable(CreateTableCommand command) {
        if (tables.containsKey(command.tableName())) {
            throw new IllegalArgumentException("table already exists: " + command.tableName());
        }

        Table table = new Table(new TableSchema(command.tableName(), command.columns()));
        tables.put(command.tableName(), table);
        save(table);
        return QueryResult.message("table created");
    }

    private QueryResult insert(InsertCommand command) {
        Table table = table(command.tableName());
        if (command.values().size() != table.schema().columns().size()) {
            throw new IllegalArgumentException("expected " + table.schema().columns().size() + " values");
        }

        Row row = new Row();
        for (int i = 0; i < command.values().size(); i++) {
            Column column = table.schema().columns().get(i);
            row.set(column.name(), Value.fromLiteral(command.values().get(i), column.type()));
        }

        table.insert(row);
        save(table);
        return QueryResult.message("1 row inserted");
    }

    private QueryResult select(SelectCommand command) {
        Table table = table(command.tableName());
        List<String> columns = command.columns().equals(List.of("*"))
                ? table.schema().columnNames()
                : validateColumns(table, command.columns());

        SearchResult search = command.condition()
                .map(table::search)
                .orElseGet(() -> new SearchResult(table.rows(), false));
        List<Row> matches = search.rows();

        List<List<String>> resultRows = new ArrayList<>();
        for (Row row : matches) {
            List<String> rendered = new ArrayList<>();
            for (String column : columns) {
                rendered.add(row.get(column).display());
            }
            resultRows.add(rendered);
        }

        String searchMode = search.usedIndex() ? "indexed search" : "linear scan";
        return QueryResult.table(columns, resultRows, searchMode);
    }

    private QueryResult update(UpdateCommand command) {
        Table table = table(command.tableName());
        Column column = table.schema().column(command.column());
        Value value = Value.fromLiteral(command.value(), column.type());

        List<Row> matches = table.search(command.condition()).rows();
        for (Row row : matches) {
            table.update(row, command.column(), value);
        }

        save(table);
        return QueryResult.message(matches.size() + " rows updated");
    }

    private QueryResult delete(DeleteCommand command) {
        Table table = table(command.tableName());
        List<Row> matches = table.search(command.condition()).rows();
        for (Row row : matches) {
            table.delete(row);
        }

        save(table);
        return QueryResult.message(matches.size() + " rows deleted");
    }

    private Table table(String name) {
        Table table = tables.get(name);
        if (table == null) {
            throw new IllegalArgumentException("unknown table: " + name);
        }
        return table;
    }

    private List<String> validateColumns(Table table, List<String> columns) {
        for (String column : columns) {
            table.schema().column(column);
        }
        return columns;
    }

    private void save(Table table) {
        try {
            storage.save(table);
        } catch (IOException ex) {
            throw new IllegalStateException("could not save table: " + ex.getMessage(), ex);
        }
    }
}
