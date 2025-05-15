package stud.atmt.atmtlite;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NFA {
    private Set<String> states = new HashSet<>();
    private Set<String> acceptingStates = new HashSet<>();
    private Set<Character> alphabet = new HashSet<>();
    private Map<String, Map<Character, Set<String>>> transitions = new HashMap<>();
    private String startState = "S";
    private String finalState = "$";

    public Map<String, Map<Character, Set<String>>> getTransitions() {
        return transitions;
    }

    public String getStartState() {
        return startState;
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public Set<String> getAcceptingStates() {
        return acceptingStates;
    }

    public Set<String> getStates() {
        return states;
    }

    public void addTransition(String from, char symbol, String to) {
        states.add(from);
        states.add(to);
        alphabet.add(symbol);

        transitions
                .computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(symbol, k -> new HashSet<>())
                .add(to);  // << добавляем to в множество состояний
    }

    public void addAcceptingState(String state) {
        states.add(state);
        acceptingStates.add(state);
    }

    public void buildNFA(List<Rule> ruleList, String firstSymbol) {

        this.addAcceptingState(finalState);

        for (Rule rule : ruleList) {
            if (rule.getValue().length() > 1) {
                this.addTransition(rule.getKey(), rule.getValue().charAt(1),
                        String.valueOf(rule.getValue().charAt(0)));
            } else {
                this.addTransition(rule.getKey(), rule.getValue().charAt(0), finalState);
            }
        }
    }

    public DFA toDFA() {
        DFA dfa = new DFA();
        dfa.setStartState(startState); // Пока запомним что стартует с "S"

        Map<Set<String>, String> setToStateName = new HashMap<>(); // Множество -> имя состояния
        Map<String, Set<String>> stateNameToSet = new HashMap<>(); // Имя состояния -> множество

        int stateCounter = 0; // Для именования новых состояний
        Set<Set<String>> visited = new HashSet<>();
        Queue<Set<String>> queue = new LinkedList<>();

        Set<String> startSet = Set.of(startState);
        queue.add(startSet);
        setToStateName.put(startSet, startState); // Назовём стартовое состояние просто "S"
        stateNameToSet.put(startState, startSet);

        while (!queue.isEmpty()) {
            Set<String> currentSet = queue.poll();
            String currentName = setToStateName.get(currentSet);

            for (char symbol : alphabet) {
                Set<String> nextSet = new HashSet<>();

                // Собираем все состояния, достижимые по symbol из currentSet
                for (String state : currentSet) {
                    Map<Character, Set<String>> map = transitions.get(state);
                    if (map != null && map.containsKey(symbol)) {
                        nextSet.addAll(map.get(symbol));
                    }
                }

                if (nextSet.isEmpty()) continue; // Нет переходов по этому символу

                // Если это новое множество состояний
                if (!setToStateName.containsKey(nextSet)) {
                    String newName = "Q" + (stateCounter++);
                    setToStateName.put(nextSet, newName);
                    stateNameToSet.put(newName, nextSet);
                    queue.add(nextSet);
                }

                String nextName = setToStateName.get(nextSet);
                dfa.addTransition(currentName, symbol, nextName);
            }
        }

        // Отметить финальные состояния
        for (var entry : stateNameToSet.entrySet()) {
            String name = entry.getKey();
            Set<String> nfaStates = entry.getValue();
            for (String s : nfaStates) {
                if (acceptingStates.contains(s)) {
                    dfa.addAcceptingState(name);
                    break;
                }
            }
        }

        return dfa;
    }


    private boolean containsAccepting(Set<String> states) {
        for (String state : states) {
            if (acceptingStates.contains(state)) {
                return true;
            }
        }
        return false;
    }

    public void addEpsilonTransition(String from, String to) {
        states.add(from);
        states.add(to);
        transitions
                .computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(null, k -> new HashSet<>()) // null для ε-переходов
                .add(to);
    }

    // Метод проверки принадлежности цепочки
    public boolean accepts(String input) {
        Set<String> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char ch : input.toCharArray()) {
            Set<String> nextStates = new HashSet<>();
            for (String state : currentStates) {
                Map<Character, Set<String>> map = transitions.get(state);
                if (map != null && map.containsKey(ch)) {
                    nextStates.addAll(map.get(ch));
                }
            }
            if (nextStates.isEmpty()) return false;
            currentStates = nextStates;
        }

        for (String state : currentStates) {
            if (acceptingStates.contains(state)) {
                return true;
            }
        }
        return false;
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

    public List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();

        // Обрабатываем все обычные переходы (по символам алфавита)
        for (Map.Entry<String, Map<Character, Set<String>>> entry : transitions.entrySet()) {
            String fromState = entry.getKey();
            Map<Character, Set<String>> symbolTransitions = entry.getValue();

            for (Map.Entry<Character, Set<String>> transition : symbolTransitions.entrySet()) {
                Character symbol = transition.getKey();
                // Пропускаем ε-переходы (их обработаем отдельно)
                if (symbol == null) continue;

                Set<String> toStates = transition.getValue();

                for (String toState : toStates) {
                    // Правило вида: fromState → symbol toState
                    rules.add(new Rule(fromState, symbol + toState));

                    // Если целевое состояние финальное, добавляем правило fromState → symbol
                    if (acceptingStates.contains(toState)) {
                        rules.add(new Rule(fromState, String.valueOf(symbol)));
                    }
                }
            }
        }

        // Обрабатываем ε-переходы
        for (Map.Entry<String, Map<Character, Set<String>>> entry : transitions.entrySet()) {
            String fromState = entry.getKey();
            Map<Character, Set<String>> symbolTransitions = entry.getValue();

            if (symbolTransitions.containsKey(null)) {
                Set<String> epsilonTransitions = symbolTransitions.get(null);

                for (String toState : epsilonTransitions) {
                    // Добавляем ε-правило: fromState → toState
                    rules.add(new Rule(fromState, toState));

                    // Если целевое состояние финальное, добавляем возможность завершения
                    if (acceptingStates.contains(toState)) {
                        rules.add(new Rule(fromState, ""));
                    }
                }
            }
        }

        // Добавляем правила для финальных состояний
        for (String acceptingState : acceptingStates) {
            // Позволяем завершать в любом финальном состоянии
            rules.add(new Rule(acceptingState, ""));
        }

        return rules;
    }
}
