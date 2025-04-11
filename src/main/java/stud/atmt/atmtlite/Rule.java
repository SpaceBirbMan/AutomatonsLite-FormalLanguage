package stud.atmt.atmtlite;

import java.util.ArrayList;
import java.util.Objects;

public class Rule {
    private String key; // Левая часть правила
    private String value; // Правая часть правила
    private boolean isLooped; // Флаг зацикливания (выставляется только в одном методе и там же и используется)

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

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rule rule = (Rule) obj;
        return key.equals(rule.key) && value.equals(rule.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + " -> " + value;
    }
}