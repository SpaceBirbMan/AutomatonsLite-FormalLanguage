package stud.atmt.atmtlite;

import java.util.*;

public class Grammar {
    private List<Rule> rules; // Правила
    private String startSymbol; // Начальный символ

    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Grammar(List<String> nonterminal, List<String> terminal, List<Rule> rules, String startSymbol) {
//        this.nonterminal = nonterminal;
//        this.terminal = terminal;
        this.rules = rules;
        this.startSymbol = startSymbol;
    }

    public Grammar(List<String> nonterminal, List<String> terminal, List<Rule> rules) {
        this(nonterminal, terminal, rules, "S");
    }

    public Grammar(List<Rule> rules) {
        this(new ArrayList<>(), new ArrayList<>(), rules, "S");
    }

    public String getTypeGrammar() {
        boolean isRegular = true;
        boolean isContextFree = true;
        boolean isContextSensitive = true;

        for (Rule rule : rules) {
            String lhs = rule.getKey();
            String rhs = rule.getValue();

            // Проверка на регулярность
            if (!lhs.matches("[A-Z]") || !rhs.matches("[a-z]*[A-Z]?[a-z]*")) {
                isRegular = false;
            }

            // Проверка на контекстно-свободность
            if (!lhs.matches("[A-Z]")) {
                isContextFree = false;
            }

            // Проверка на контекстно-зависимость
            if (lhs.length() > rhs.length()) {
                isContextSensitive = false;
            }
        }

        if (isRegular) {
            return "Тип 3 — Регулярная грамматика";
        } else if (isContextFree) {
            return "Тип 2 — Контекстно-свободная грамматика";
        } else if (isContextSensitive) {
            return "Тип 1 — Контекстно-зависимая грамматика";
        } else {
            return "Тип 0 — Неограниченная грамматика";
        }
    }

    public List<String> makeSequence(String text) {
        int maxCount = 10000;
        int count = 0;
        List<String> sequence = new ArrayList<>();
        sequence.add(text);

        while (count < maxCount) {
            boolean changed = false;

            for (Rule rule : rules) {
                String key = rule.getKey();
                String value = rule.getValue();

                int pos = text.lastIndexOf(value);
                if (pos != -1) {
                    text = text.substring(0, pos) + key + text.substring(pos + value.length());
                    sequence.add(text);
                    changed = true;
                }
            }

            if (!changed) break;
            count++;
        }

        Collections.reverse(sequence);

        return sequence;
    }

    public ArrayList<ArrayList<String>> makeSequenceMap(String target) {
        ArrayList<ArrayList<String>> allSequences = new ArrayList<>();
        Queue<ArrayList<String>> queue = new LinkedList<>();

        // Начинаем с начального символа
        ArrayList<String> startSequence = new ArrayList<>();
        startSequence.add(startSymbol);
        queue.add(startSequence);

        while (!queue.isEmpty()) {
            ArrayList<String> currentSequence = queue.poll();
            String currentString = currentSequence.get(currentSequence.size() - 1);

            if (currentString.equals(target)) {
                allSequences.add(new ArrayList<>(currentSequence));
                continue;
            }

            // Подставляем все возможные правила
            for (Rule rule : rules) {
                String key = rule.getKey();
                String value = rule.getValue();

                // Проверяем все вхождения key в строке
                int pos = currentString.indexOf(key);
                while (pos != -1) {
                    String newString = currentString.substring(0, pos) + value + currentString.substring(pos + key.length());

                    // Создаём новый путь подстановок
                    ArrayList<String> newSequence = new ArrayList<>(currentSequence);
                    newSequence.add(newString);
                    queue.add(newSequence);

                    // Ищем дальше
                    pos = currentString.indexOf(key, pos + 1);
                }
            }
        }

        return allSequences;
    }

    public String describeLanguage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("L = {");
        for (Rule rule : rules) {

        }
        stringBuilder.append("|");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}