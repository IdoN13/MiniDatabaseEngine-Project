package db.model;

public record Condition(String column, Operator operator, String literal) {
}
