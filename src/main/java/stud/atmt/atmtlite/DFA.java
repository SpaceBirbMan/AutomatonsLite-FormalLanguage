package stud.atmt.atmtlite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFA {
    private Set<String> states = new HashSet<>();
    private Set<String> acceptingStates = new HashSet<>();
    private Set<Character> alphabet = new HashSet<>();
    private Map<String, Map<Character, String>> transitions = new HashMap<>();
    private String startState = "S";

    public void addTransition(String from, char symbol, String to) {
        states.add(from);
        states.add(to);
        alphabet.add(symbol);
        transitions
                .computeIfAbsent(from, k -> new HashMap<>())
                .put(symbol, to);
    }

    public void addAcceptingState(String state) {
        states.add(state);
        acceptingStates.add(state);
    }

    public boolean accepts(String input) {
        String current = startState;
        for (char ch : input.toCharArray()) {
            Map<Character, String> map = transitions.get(current);
            if (map == null || !map.containsKey(ch)) return false;
            current = map.get(ch);
        }
        return acceptingStates.contains(current);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Start: ").append(startState).append("\n");
        sb.append("Accepting: ").append(acceptingStates).append("\n");
        sb.append("Transitions:\n");
        for (var entry : transitions.entrySet()) {
            for (var sub : entry.getValue().entrySet()) {
                sb.append("  ").append(entry.getKey()).append(" --")
                        .append(sub.getKey()).append("--> ").append(sub.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}
