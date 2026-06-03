package db.model;

public record Value(DataType type, Object raw) implements Comparable<Value> {
    public static Value fromLiteral(String literal, DataType expectedType) {
        String text = literal.trim();

        return switch (expectedType) {
            case INT -> new Value(DataType.INT, Integer.parseInt(text));
            case BOOLEAN -> {
                if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("invalid BOOLEAN value: " + literal);
                }
                yield new Value(DataType.BOOLEAN, Boolean.parseBoolean(text));
            }
            case STRING -> {
                if (text.length() < 2 || text.charAt(0) != '"' || text.charAt(text.length() - 1) != '"') {
                    throw new IllegalArgumentException("invalid STRING value: " + literal);
                }
                yield new Value(DataType.STRING, text.substring(1, text.length() - 1));
            }
        };
    }

    public String display() {
        return String.valueOf(raw);
    }

    public String persist() {
        return switch (type) {
            case STRING -> raw.toString().replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n");
            default -> raw.toString();
        };
    }

    public static Value fromPersisted(String text, DataType type) {
        return switch (type) {
            case INT -> new Value(type, Integer.parseInt(text));
            case BOOLEAN -> new Value(type, Boolean.parseBoolean(text));
            case STRING -> new Value(type, text.replace("\\n", "\n").replace("\\p", "|").replace("\\\\", "\\"));
        };
    }

    @Override
    public int compareTo(Value other) {
        if (type != other.type) {
            throw new IllegalArgumentException("cannot compare different value types");
        }
        return switch (type) {
            case INT -> Integer.compare((Integer) raw, (Integer) other.raw);
            case STRING -> raw.toString().compareTo(other.raw.toString());
            case BOOLEAN -> Boolean.compare((Boolean) raw, (Boolean) other.raw);
        };
    }
}
