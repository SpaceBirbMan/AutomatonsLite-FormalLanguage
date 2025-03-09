package stud.atmt.atmtlite;

import java.util.ArrayList;

public class Rule {
    private String key; // Левая часть правила
    private String value; // Правая часть правила
    private boolean isLooped; // Флаг зацикливания

    public Rule(String key, String value, boolean isLooped) {
        this.key = key;
        this.value = value;
        this.isLooped = isLooped;
    }

    public Rule(String key, String value) {
        this(key, value, false);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isLooped() {
        return isLooped;
    }

    public void setLooped(boolean looped) {
        isLooped = looped;
    }

    @Override
    public String toString() {
        return key + " -> " + value;
    }
}