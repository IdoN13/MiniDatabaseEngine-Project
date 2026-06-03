package db.parser.command;

import db.model.Condition;

import java.util.List;
import java.util.Optional;

public record SelectCommand(String tableName, List<String> columns, Condition rawCondition) implements Command {
    public Optional<Condition> condition() {
        return Optional.ofNullable(rawCondition);
    }
}
