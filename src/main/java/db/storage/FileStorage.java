package db.storage;

import db.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileStorage {
    private final Path dataDir;

    public FileStorage(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Map<String, Table> loadAll() throws IOException {
        Map<String, Table> tables = new LinkedHashMap<>();
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
            return tables;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDir, "*.table")) {
            for (Path file : files) {
                Table table = load(file);
                tables.put(table.schema().name(), table);
            }
        }
        return tables;
    }

    public void save(Table table) throws IOException {
        Files.createDirectories(dataDir);
        Path file = dataDir.resolve(table.schema().name() + ".table");
        List<String> lines = new ArrayList<>();

        List<String> header = new ArrayList<>();
        for (Column column : table.schema().columns()) {
            header.add(column.name() + ":" + column.type());
        }
        lines.add(String.join("|", header));

        for (Row row : table.rows()) {
            List<String> values = new ArrayList<>();
            for (Column column : table.schema().columns()) {
                values.add(row.get(column.name()).persist());
            }
            lines.add(String.join("|", values));
        }

        Files.write(file, lines);
    }

    private Table load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            throw new IOException("empty table file: " + file);
        }

        String name = file.getFileName().toString().replaceFirst("\\.table$", "");
        List<Column> columns = new ArrayList<>();
        for (String part : lines.get(0).split("\\|")) {
            String[] pair = part.split(":");
            columns.add(new Column(pair[0], DataType.valueOf(pair[1])));
        }

        Table table = new Table(new TableSchema(name, columns));
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|", -1);
            Row row = new Row();
            for (int j = 0; j < columns.size(); j++) {
                Column column = columns.get(j);
                row.set(column.name(), Value.fromPersisted(parts[j], column.type()));
            }
            table.insert(row);
        }
        return table;
    }
}
