package stud.atmt.atmtlite;

import java.util.*;

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

    public void setStartState(String startState) {
        this.startState = startState;
        states.add(startState);
    }

    public boolean hasState(String state) {
        return states.contains(state);
    }

    public List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();

        // For each transition in the DFA, create a corresponding rule
        for (Map.Entry<String, Map<Character, String>> entry : transitions.entrySet()) {
            String fromState = entry.getKey();
            Map<Character, String> stateTransitions = entry.getValue();

            for (Map.Entry<Character, String> transition : stateTransitions.entrySet()) {
                char symbol = transition.getKey();
                String toState = transition.getValue();

                // Create the right-hand side of the rule (symbol + toState)
                String rhs = symbol + toState;

                // If the target state is accepting, also add a rule that ends with just the symbol
                if (acceptingStates.contains(toState)) {
                    rules.add(new Rule(fromState, String.valueOf(symbol)));
                }

                rules.add(new Rule(fromState, rhs));
            }
        }

        // If the start state is also an accepting state, add epsilon production
        if (acceptingStates.contains(startState)) {
            rules.add(new Rule(startState, ""));
        }

        return rules;
    }

    public DFA minimize() {
        // Удаление недостижимых состояний
        Set<String> reachableStates = findReachableStates();
        DFA reachableDFA = createSubDFA(reachableStates);

        // Распил принимающих и не принимающих
        Set<String> accepting = new HashSet<>(reachableDFA.acceptingStates);
        Set<String> nonAccepting = new HashSet<>(reachableDFA.states);
        nonAccepting.removeAll(accepting);

        List<Set<String>> partitions = new ArrayList<>();
        if (!nonAccepting.isEmpty()) {
            partitions.add(new HashSet<>(nonAccepting));
        }
        if (!accepting.isEmpty()) {
            partitions.add(new HashSet<>(accepting));
        }


        boolean changed;
        do {
            changed = false;
            List<Set<String>> newPartitions = new ArrayList<>();

            for (Set<String> partition : partitions) {
                if (partition.size() == 1) {
                    newPartitions.add(partition);
                    continue;
                }

                Map<String, Set<String>> signatureToStates = new HashMap<>();

                for (String state : partition) {
                    String signature = computeSignature(state, partitions);
                    signatureToStates.computeIfAbsent(signature, k -> new HashSet<>()).add(state);
                }

                if (signatureToStates.size() > 1) {
                    changed = true;
                }
                newPartitions.addAll(signatureToStates.values());
            }

            partitions = newPartitions;
        } while (changed);

        DFA minimizedDFA = new DFA();

        // Новые имена
        Map<String, String> stateToPartition = new HashMap<>();
        for (Set<String> partition : partitions) {
            String newState = String.join("", partition);
            for (String state : partition) {
                stateToPartition.put(state, newState);
            }

            for (String state : partition) {
                if (reachableDFA.acceptingStates.contains(state)) {
                    minimizedDFA.addAcceptingState(newState);
                    break;
                }
            }

            if (partition.contains(reachableDFA.startState)) {
                minimizedDFA.setStartState(newState);
            }
        }

        for (Set<String> partition : partitions) {
            String fromState = stateToPartition.get(partition.iterator().next());

            Map<Character, String> transitions = reachableDFA.transitions.get(partition.iterator().next());
            if (transitions != null) {
                for (Map.Entry<Character, String> entry : transitions.entrySet()) {
                    char symbol = entry.getKey();
                    String toState = entry.getValue();
                    String newToState = stateToPartition.get(toState);
                    minimizedDFA.addTransition(fromState, symbol, newToState);
                }
            }
        }

        return minimizedDFA;
    }

    private String computeSignature(String state, List<Set<String>> partitions) {
        StringBuilder signature = new StringBuilder();
        Map<Character, String> transitions = this.transitions.get(state);

        for (char symbol : this.alphabet) {
            String target = transitions != null ? transitions.get(symbol) : null;
            if (target == null) {
                signature.append("null");
            } else {
                for (int i = 0; i < partitions.size(); i++) {
                    if (partitions.get(i).contains(target)) {
                        signature.append(i);
                        break;
                    }
                }
            }
            signature.append(",");
        }
        return signature.toString();
    }

    private Set<String> findReachableStates() {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(this.startState);
        reachable.add(this.startState);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<Character, String> transitions = this.transitions.get(current);

            if (transitions != null) {
                for (String target : transitions.values()) {
                    if (!reachable.contains(target)) {
                        reachable.add(target);
                        queue.add(target);
                    }
                }
            }
        }

        return reachable;
    }

    private DFA createSubDFA(Set<String> statesToKeep) {
        DFA subDFA = new DFA();

        // Set start state
        if (statesToKeep.contains(this.startState)) {
            subDFA.setStartState(this.startState);
        }

        // Add accepting states
        for (String state : this.acceptingStates) {
            if (statesToKeep.contains(state)) {
                subDFA.addAcceptingState(state);
            }
        }

        // Add transitions
        for (String fromState : statesToKeep) {
            Map<Character, String> transitions = this.transitions.get(fromState);
            if (transitions != null) {
                for (Map.Entry<Character, String> entry : transitions.entrySet()) {
                    if (statesToKeep.contains(entry.getValue())) {
                        subDFA.addTransition(fromState, entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        return subDFA;
    }

    public Set<String> getStates() {
        return states;
    }

    public Set<String> getAcceptingStates() {
        return acceptingStates;
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public String getStartState() {
        return startState;
    }

    public Map<String, Map<Character, String>> getTransitions() {
        return transitions;
    }
}


