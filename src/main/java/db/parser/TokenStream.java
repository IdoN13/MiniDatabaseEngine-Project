package db.parser;

import java.util.List;

class TokenStream {
    private final List<String> tokens;
    private int index;

    TokenStream(List<String> tokens) {
        this.tokens = tokens;
    }

    String peek() throws ParseException {
        if (index >= tokens.size()) {
            throw new ParseException("unexpected end of input");
        }
        return tokens.get(index);
    }

    String peekUpper() throws ParseException {
        return peek().toUpperCase();
    }

    boolean accept(String expected) {
        if (index < tokens.size() && tokens.get(index).equalsIgnoreCase(expected)) {
            index++;
            return true;
        }
        return false;
    }

    void expect(String expected) throws ParseException {
        if (!accept(expected)) {
            throw new ParseException("expected '" + expected + "'");
        }
    }

    void expectEnd() throws ParseException {
        if (index != tokens.size()) {
            throw new ParseException("unexpected token: " + tokens.get(index));
        }
    }

    String identifier() throws ParseException {
        String token = peek();
        if (!token.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new ParseException("expected identifier, got " + token);
        }
        index++;
        return token;
    }

    String value() throws ParseException {
        String token = peek();
        if (token.equals(",") || token.equals(")") || token.equals(";")) {
            throw new ParseException("expected value");
        }
        index++;
        return token;
    }

    String operator() throws ParseException {
        String token = peek();
        if (!List.of("=", "!=", ">", "<", ">=", "<=").contains(token)) {
            throw new ParseException("expected comparison operator");
        }
        index++;
        return token;
    }
}
