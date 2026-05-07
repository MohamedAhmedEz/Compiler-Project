package Interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolTable {

    private final Map<String, Integer> values      = new HashMap<>();
    private final Map<String, Boolean> initialised = new HashMap<>();

    public void set(String name, int value) {
        values.put(name, value);
        initialised.put(name, true);
    }

    public int get(String name) {
        if (!initialised.getOrDefault(name, false)) {
            throw new RuntimeError("Variable '" + name
                    + "' used before initialisation");
        }
        return values.get(name);
    }

    public boolean isInitialised(String name) {
        return initialised.getOrDefault(name, false);
    }

    public Set<String> allVariables() {
        return values.keySet();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}