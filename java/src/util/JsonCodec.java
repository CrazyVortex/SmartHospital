package util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonCodec {
    private static final int MAX_NESTING_DEPTH = 32;

    private JsonCodec() {
    }

    public static Object parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON istegi bos olamaz.");
        }
        Parser parser = new Parser(json);
        Object value = parser.readValue(0);
        parser.skipWhitespace();
        if (!parser.isAtEnd()) {
            throw new IllegalArgumentException("JSON sonunda beklenmeyen veri var.");
        }
        return value;
    }

    public static String stringify(Object value) {
        StringBuilder json = new StringBuilder();
        appendValue(json, value);
        return json.toString();
    }

    private static void appendValue(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String) {
            appendString(json, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Map<?, ?>) {
            appendObject(json, (Map<?, ?>) value);
        } else if (value instanceof Iterable<?>) {
            appendArray(json, (Iterable<?>) value);
        } else {
            throw new IllegalArgumentException("JSON icin desteklenmeyen veri tipi.");
        }
    }

    private static void appendObject(StringBuilder json, Map<?, ?> values) {
        json.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("JSON nesne anahtarlari metin olmalidir.");
            }
            if (!first) {
                json.append(',');
            }
            appendString(json, (String) entry.getKey());
            json.append(':');
            appendValue(json, entry.getValue());
            first = false;
        }
        json.append('}');
    }

    private static void appendArray(StringBuilder json, Iterable<?> values) {
        json.append('[');
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                json.append(',');
            }
            appendValue(json, value);
            first = false;
        }
        json.append(']');
    }

    private static void appendString(StringBuilder json, String value) {
        json.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
            }
        }
        json.append('"');
    }

    private static final class Parser {
        private final String input;
        private int position;

        private Parser(String input) {
            this.input = input;
        }

        private Object readValue(int depth) {
            skipWhitespace();
            if (isAtEnd()) {
                throw invalidJson();
            }
            if (depth > MAX_NESTING_DEPTH) {
                throw new IllegalArgumentException("JSON ic ice gecme siniri asildi.");
            }

            char character = input.charAt(position);
            if (character == '{') {
                return readObject(depth);
            }
            if (character == '[') {
                return readArray(depth);
            }
            if (character == '"') {
                return readString();
            }
            if (character == 't') {
                return readLiteral("true", Boolean.TRUE);
            }
            if (character == 'f') {
                return readLiteral("false", Boolean.FALSE);
            }
            if (character == 'n') {
                return readLiteral("null", null);
            }
            if (character == '-' || isDigit(character)) {
                return readNumber();
            }
            throw invalidJson();
        }

        private Map<String, Object> readObject(int depth) {
            Map<String, Object> values = new LinkedHashMap<>();
            position++;
            skipWhitespace();
            if (consume('}')) {
                return values;
            }

            while (true) {
                skipWhitespace();
                if (isAtEnd() || input.charAt(position) != '"') {
                    throw invalidJson();
                }
                String key = readString();
                skipWhitespace();
                if (!consume(':')) {
                    throw invalidJson();
                }
                if (values.containsKey(key)) {
                    throw new IllegalArgumentException("JSON nesnesinde yinelenen alan adi var.");
                }
                values.put(key, readValue(depth + 1));
                skipWhitespace();
                if (consume('}')) {
                    return values;
                }
                if (!consume(',')) {
                    throw invalidJson();
                }
            }
        }

        private List<Object> readArray(int depth) {
            List<Object> values = new ArrayList<>();
            position++;
            skipWhitespace();
            if (consume(']')) {
                return values;
            }

            while (true) {
                values.add(readValue(depth + 1));
                skipWhitespace();
                if (consume(']')) {
                    return values;
                }
                if (!consume(',')) {
                    throw invalidJson();
                }
            }
        }

        private String readString() {
            position++;
            StringBuilder value = new StringBuilder();
            while (!isAtEnd()) {
                char character = input.charAt(position++);
                if (character == '"') {
                    return value.toString();
                }
                if (character < 0x20) {
                    throw invalidJson();
                }
                if (character != '\\') {
                    value.append(character);
                    continue;
                }
                if (isAtEnd()) {
                    throw invalidJson();
                }
                char escaped = input.charAt(position++);
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        value.append(escaped);
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'u':
                        value.append(readUnicodeEscape());
                        break;
                    default:
                        throw invalidJson();
                }
            }
            throw invalidJson();
        }

        private char readUnicodeEscape() {
            if (position + 4 > input.length()) {
                throw invalidJson();
            }
            int value = 0;
            for (int index = 0; index < 4; index++) {
                int digit = Character.digit(input.charAt(position++), 16);
                if (digit < 0) {
                    throw invalidJson();
                }
                value = value * 16 + digit;
            }
            return (char) value;
        }

        private BigDecimal readNumber() {
            int start = position;
            consume('-');
            if (!isAtEnd() && input.charAt(position) == '0') {
                position++;
            } else {
                readDigits();
            }
            if (consume('.')) {
                readDigits();
            }
            if (consume('e') || consume('E')) {
                if (!consume('+')) {
                    consume('-');
                }
                readDigits();
            }
            try {
                return new BigDecimal(input.substring(start, position));
            } catch (NumberFormatException exception) {
                throw invalidJson();
            }
        }

        private void readDigits() {
            int start = position;
            while (!isAtEnd() && isDigit(input.charAt(position))) {
                position++;
            }
            if (position == start) {
                throw invalidJson();
            }
        }

        private Object readLiteral(String literal, Object value) {
            if (!input.startsWith(literal, position)) {
                throw invalidJson();
            }
            position += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (!isAtEnd()) {
                char character = input.charAt(position);
                if (character != ' ' && character != '\n'
                        && character != '\r' && character != '\t') {
                    return;
                }
                position++;
            }
        }

        private boolean consume(char expected) {
            if (!isAtEnd() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private boolean isAtEnd() {
            return position >= input.length();
        }

        private IllegalArgumentException invalidJson() {
            return new IllegalArgumentException("Gecersiz JSON istegi.");
        }

        private boolean isDigit(char character) {
            return character >= '0' && character <= '9';
        }
    }
}
