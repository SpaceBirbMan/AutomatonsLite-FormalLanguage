package stud.atmt.atmtlite;

import java.util.*;

public class Grammar {
    private List<Rule> rules; // Правила
    private String startSymbol; // Начальный символ
    private HashMap<String, Integer> terminalsAndConstraints;

    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Grammar() {}

    public Grammar(HashMap<String, Integer> terminalsAndConstraints) {
        this.terminalsAndConstraints = terminalsAndConstraints;
    }

    public Grammar(List<String> nonterminal, List<String> terminal, List<Rule> rules, String startSymbol) {
//        this.nonterminal = nonterminal;
//        this.terminal = terminal;
        Bus.globalError = "";
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

    /// Генерирует цепочки очень интересно, пытаясь правила по тупому подстроить под целевую цепочку
    public List<String> makeSequence(String target) {
        int maxCount = 10000;
        int count = 0;
        List<String> sequence = new ArrayList<>();
        sequence.add(target);

        while (count < maxCount) {
            boolean changed = false;

            for (Rule rule : rules) {
                String key = rule.getKey();
                String value = rule.getValue();

                int pos = target.lastIndexOf(value);
                if (pos != -1) {
                    target = target.substring(0, pos) + key + target.substring(pos + value.length());
                    sequence.add(target);
                    changed = true;
                }
            }

            if (!changed) break;
            count++;
        }

        Collections.reverse(sequence);

        return sequence;
    }

    public String buildGrammar(GrammarApp.ParsedLanguage parsedLanguage) {
        HashMap<String, String> terminalsAndConstraints = parsedLanguage.terminalsAndConstraints();
        HashMap<String, String> terminalsGroups = parsedLanguage.terminalsGroups();
        HashMap<String, String> palindrome = parsedLanguage.palindrome();
        ArrayList<String> randomTerminals = parsedLanguage.randomTerminals();

        ArrayList<Rule> rules = new ArrayList<>();

        createRulesForStandardTerminals(terminalsAndConstraints);
        createRulesForGroups(terminalsGroups);
        createRulesForPalindrome(palindrome);
        createRulesForRandomTerminals(randomTerminals);

        return null;
    }

    private void createRulesForRandomTerminals(ArrayList<String> randomTerminals) {
        // вообще, строго говоря, можно просто сделать одно правило, которое будет определять генерацию всех рандомных терминалов
        // типа R = aR | bR | cR | dR | a | b | c | d
    }

    private void createRulesForPalindrome(HashMap<String, String> palindrome) {
    }

    private void createRulesForGroups(HashMap<String, String> terminalsGroups) {
    }

    private void createRulesForStandardTerminals(HashMap<String, String> terminalsAndConstraints) {
    }
}