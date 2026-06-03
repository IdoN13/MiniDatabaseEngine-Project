package db.parser.command;

import java.util.List;

public record InsertCommand(String tableName, List<String> values) implements Command {
}
