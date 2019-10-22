package app;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment
 */
public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        Environment t = this;
        while (true) {
            if (t == null) {
                break;
            } else {
                if (t.values.containsKey(name.lexeme)) {
                    return t.values.get(name.lexeme);
                }
                t = t.enclosing;
            }
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        Environment t = this;
        while (true) {
            if (t == null) {
                break;
            } else {
                if (t.values.containsKey(name.lexeme)) {
                    t.values.put(name.lexeme, value);
                }
                t = t.enclosing;
            }
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}