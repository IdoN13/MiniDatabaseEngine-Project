package db.parser.command;

import db.model.Condition;

public record DeleteCommand(String tableName, Condition condition) implements Command {
}
