package db.parser.command;

import db.model.Column;

import java.util.List;

public record CreateTableCommand(String tableName, List<Column> columns) implements Command {
}
