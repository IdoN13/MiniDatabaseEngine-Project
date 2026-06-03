package db.parser;

import db.model.*;
import db.parser.command.*;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {
    public Command parse(String sql) throws ParseException {
        TokenStream tokens = new TokenStream(tokenize(sql));
        String first = tokens.peekUpper();

        return switch (first) {
            case "CREATE" -> parseCreate(tokens);
            case "INSERT" -> parseInsert(tokens);
            case "SELECT" -> parseSelect(tokens);
            case "UPDATE" -> parseUpdate(tokens);
            case "DELETE" -> parseDelete(tokens);
            default -> throw new ParseException("unknown command: " + tokens.peek());
        };
    }

    private Command parseCreate(TokenStream tokens) throws ParseException {
        tokens.expect("CREATE");
        tokens.expect("TABLE");
        String table = tokens.identifier();
        tokens.expect("(");

        List<Column> columns = new ArrayList<>();
        do {
            String name = tokens.identifier();
            DataType type = DataType.valueOf(tokens.identifier().toUpperCase());
            columns.add(new Column(name, type));
        } while (tokens.accept(","));

        tokens.expect(")");
        tokens.expect(";");
        tokens.expectEnd();
        return new CreateTableCommand(table, columns);
    }

    private Command parseInsert(TokenStream tokens) throws ParseException {
        tokens.expect("INSERT");
        tokens.expect("INTO");
        String table = tokens.identifier();
        tokens.expect("VALUES");
        tokens.expect("(");

        List<String> values = new ArrayList<>();
        do {
            values.add(tokens.value());
        } while (tokens.accept(","));

        tokens.expect(")");
        tokens.expect(";");
        tokens.expectEnd();
        return new InsertCommand(table, values);
    }

    private Command parseSelect(TokenStream tokens) throws ParseException {
        tokens.expect("SELECT");
        List<String> columns = new ArrayList<>();
        if (tokens.accept("*")) {
            columns.add("*");
        } else {
            do {
                columns.add(tokens.identifier());
            } while (tokens.accept(","));
        }

        tokens.expect("FROM");
        String table = tokens.identifier();
        Condition condition = null;
        if (tokens.accept("WHERE")) {
            condition = parseCondition(tokens);
        }
        tokens.expect(";");
        tokens.expectEnd();
        return new SelectCommand(table, columns, condition);
    }

    private Command parseUpdate(TokenStream tokens) throws ParseException {
        tokens.expect("UPDATE");
        String table = tokens.identifier();
        tokens.expect("SET");
        String column = tokens.identifier();
        tokens.expect("=");
        String value = tokens.value();
        tokens.expect("WHERE");
        Condition condition = parseCondition(tokens);
        tokens.expect(";");
        tokens.expectEnd();
        return new UpdateCommand(table, column, value, condition);
    }

    private Command parseDelete(TokenStream tokens) throws ParseException {
        tokens.expect("DELETE");
        tokens.expect("FROM");
        String table = tokens.identifier();
        tokens.expect("WHERE");
        Condition condition = parseCondition(tokens);
        tokens.expect(";");
        tokens.expectEnd();
        return new DeleteCommand(table, condition);
    }

    private Condition parseCondition(TokenStream tokens) throws ParseException {
        String column = tokens.identifier();
        String op = tokens.operator();
        String value = tokens.value();
        return new Condition(column, parseOperator(op), value);
    }

    private Operator parseOperator(String text) throws ParseException {
        return switch (text) {
            case "=" -> Operator.EQ;
            case "!=" -> Operator.NE;
            case ">" -> Operator.GT;
            case "<" -> Operator.LT;
            case ">=" -> Operator.GTE;
            case "<=" -> Operator.LTE;
            default -> throw new ParseException("invalid operator: " + text);
        };
    }

    private List<String> tokenize(String sql) throws ParseException {
        List<String> pieces = new ArrayList<>();
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if ("(),;*=".indexOf(c) >= 0) {
                pieces.add(String.valueOf(c));
                i++;
            } else if (c == '!' || c == '>' || c == '<') {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == '=') {
                    pieces.add(sql.substring(i, i + 2));
                    i += 2;
                } else {
                    pieces.add(String.valueOf(c));
                    i++;
                }
            } else if (c == '"') {
                int start = i++;
                while (i < sql.length() && sql.charAt(i) != '"') {
                    i++;
                }
                if (i >= sql.length()) {
                    throw new ParseException("unterminated string");
                }
                pieces.add(sql.substring(start, ++i));
            } else {
                int start = i;
                while (i < sql.length() && !Character.isWhitespace(sql.charAt(i)) && "(),;*!=<>".indexOf(sql.charAt(i)) < 0) {
                    i++;
                }
                pieces.add(sql.substring(start, i));
            }
        }
        return pieces;
    }
}
