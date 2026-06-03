package db.parser.command;

import db.model.Condition;

public record UpdateCommand(String tableName, String column, String value, Condition condition) implements Command {
}
