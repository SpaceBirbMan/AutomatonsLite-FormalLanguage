package stud.atmt.atmtlite;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String buildGrammar(boolean optimizationFlag, GrammarApp.ParsedLanguage parsedLanguage) {
        if (parsedLanguage == null) return "";
        HashMap<String, String> terminalsAndConstraints = parsedLanguage.terminalsAndConstraints();
        HashMap<String, String> terminalsGroups = parsedLanguage.terminalsGroups();
        HashMap<String, String> palindrome = parsedLanguage.palindrome();
        ArrayList<String> randomTerminals = parsedLanguage.randomTerminals();

        ArrayList<Rule> rules = new ArrayList<>();

        // Начальный нетерминал
        String startNonTerminal = "S";

        // Генерация правил
        rules.addAll(createRulesForStandardTerminals(terminalsAndConstraints, startNonTerminal));
        rules.addAll(createRulesForGroups(terminalsGroups, startNonTerminal));
        rules.addAll(createRulesForPalindrome(palindrome, startNonTerminal));
        rules.addAll(createRulesForRandomTerminals(randomTerminals, startNonTerminal));

        if (optimizationFlag) {
            rules = optimizeRules(rules); // оптимизация, собирает правила в кучу, изменяя вариант написания выбора путей
            // S = A S = B -> S = A | B
        }

        // Сортируем правила
        rules = sortRules(rules);

        // Преобразуем правила в строку
        StringBuilder grammar = new StringBuilder();
        for (Rule rule : rules) {
            grammar.append(rule.getKey()).append(" = ").append(rule.getValue()).append("\n");
        }

        return grammar.toString();
    }

    private ArrayList<Rule> optimizeRules(ArrayList<Rule> rules) {
        ArrayList<Rule> uniqueRules = new ArrayList<>();
        for (Rule rule : rules) {
            if (!uniqueRules.contains(rule)) {
                uniqueRules.add(rule);
            }
        }

        HashMap<String, ArrayList<String>> ruleMap = new HashMap<>();
        for (Rule rule : uniqueRules) {
            String key = rule.getKey();
            String value = rule.getValue();
            if (!ruleMap.containsKey(key)) {
                ruleMap.put(key, new ArrayList<>());
            }
            ruleMap.get(key).add(value);
        }

        ArrayList<Rule> mergedRules = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : ruleMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> values = entry.getValue();
            String mergedValue = String.join(" | ", values);
            mergedRules.add(new Rule(key, mergedValue));
        }

        HashMap<String, String> terminalReplacements = new HashMap<>();
        for (Rule rule : mergedRules) {
            if (rule.getValue().matches("[a-z]+")) { // Если правило имеет вид A = a
                terminalReplacements.put(rule.getKey(), rule.getValue());
            }
        }

        ArrayList<Rule> optimizedRules = new ArrayList<>();
        for (Rule rule : mergedRules) {
            String key = rule.getKey();
            String value = rule.getValue();

            for (Map.Entry<String, String> replacement : terminalReplacements.entrySet()) {
                value = value.replace(replacement.getKey(), replacement.getValue());
            }

            if (!value.equals(key)) { // Исключаем правила вида A = A
                optimizedRules.add(new Rule(key, value));
            }
        }

        ArrayList<Rule> finalRules = new ArrayList<>();
        for (Rule rule : optimizedRules) {
            if (!rule.getValue().equals("ε") || isNonTerminalUsed(rule.getKey(), optimizedRules)) {
                finalRules.add(rule);
            }
        }

        for (Rule rule : finalRules) {
            String key = rule.getKey();
            String value = rule.getValue();

            String[] options = value.split(" \\| ");
            ArrayList<String> recursiveOptions = new ArrayList<>(); // Варианты с рекурсией
            ArrayList<String> endingOptions = new ArrayList<>();   // Оканчивающие варианты

            for (String option : options) {
                if (option.contains(key)) { // Если вариант содержит рекурсию
                    recursiveOptions.add(option);
                } else {
                    endingOptions.add(option);
                }
            }

            if (endingOptions.size() > 1) {
                String mergedEndings = String.join(" ", endingOptions); // Объединяем через пробел
                recursiveOptions.add(mergedEndings); // Добавляем объединённый вариант
                rule.setValue(String.join(" | ", recursiveOptions)); // Обновляем правило
            }
        }

        return finalRules;
    }

    private boolean isNonTerminalUsed(String nonTerminal, ArrayList<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.getValue().contains(nonTerminal)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Rule> sortRules(ArrayList<Rule> rules) {
        // Сортируем правила по алфавиту, начиная с S
        rules.sort((rule1, rule2) -> {
            String key1 = rule1.getKey();
            String key2 = rule2.getKey();

            // S всегда идёт первым
            if (key1.equals("S")) return -1;
            if (key2.equals("S")) return 1;

            // Остальные правила сортируем по алфавиту
            return key1.compareTo(key2);
        });

        return rules;
    }

    private ArrayList<Rule> createRulesForRandomTerminals(ArrayList<String> randomTerminals, String startNonTerminal) {
        ArrayList<Rule> rules = new ArrayList<>();

        StringBuilder ruleValue = new StringBuilder();
        for (String terminal : randomTerminals) {
            ruleValue.append(terminal).append(" ").append(startNonTerminal).append(" | ");
        }
        ruleValue.append("ε"); // ε — пустая строка

        rules.add(new Rule(startNonTerminal, ruleValue.toString()));

        return rules;
    }

    private ArrayList<Rule> createRulesForPalindrome(HashMap<String, String> palindrome, String startNonTerminal) {
        ArrayList<Rule> rules = new ArrayList<>();

        for (Map.Entry<String, String> entry : palindrome.entrySet()) {
            String palindromeVars = entry.getKey();
            String constraint = entry.getValue();

            if (constraint.equals("0")) {

                rules.add(new Rule(startNonTerminal, palindromeVars));
            } else {

                String[] values = constraint.replaceAll("\\{|\\}", "").split(",");

                String palindromeNonTerminal = getNextNonTerminal();

                rules.add(new Rule(startNonTerminal, palindromeNonTerminal));

                for (String value : values) {
                    rules.add(new Rule(palindromeNonTerminal, value + " " + palindromeNonTerminal + " " + value + " | " + value + " | ε"));
                }
            }
        }

        return rules;
    }

    private ArrayList<Rule> createRulesForGroups(HashMap<String, String> terminalsGroups, String startNonTerminal) {
        ArrayList<Rule> rules = new ArrayList<>();
        String currentNonTerminal = startNonTerminal;

        for (Map.Entry<String, String> entry : terminalsGroups.entrySet()) {
            String group = entry.getKey();
            String constraint = entry.getValue();


            String[] parts = constraint.split(" ");
            String constraintVar = parts[0];
            String operator = parts[1];
            String value = parts[2];

            if (operator.equals("=")) {

                int count = Integer.parseInt(value);
                StringBuilder ruleValue = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    ruleValue.append(group).append(" ");
                }
                String nextNonTerminal = getNextNonTerminal();
                rules.add(new Rule(currentNonTerminal, ruleValue.toString().trim() + " " + nextNonTerminal));
                rules.add(new Rule(nextNonTerminal, "ε"));
                currentNonTerminal = nextNonTerminal;
            } else if (operator.equals(">") || operator.equals(">=")) {

                int minCount = Integer.parseInt(value);
                if (operator.equals(">")) {
                    minCount += 1;
                }

                // Генерируем правило для минимального количества
                StringBuilder ruleValue = new StringBuilder();
                for (int i = 0; i < minCount; i++) {
                    ruleValue.append(group).append("");
                }
                String nextNonTerminal = getNextNonTerminal();
                ruleValue.append(nextNonTerminal);
                rules.add(new Rule(currentNonTerminal, ruleValue.toString().trim()));


                rules.add(new Rule(nextNonTerminal, group + " " + nextNonTerminal + " | " + group));
                currentNonTerminal = nextNonTerminal;
            }
        }

        return rules;
    }

    private char currentNonTerminal = 'A';

    private String getNextNonTerminal() {
        if (currentNonTerminal > 'Z') {
            throw new IllegalStateException("Недостаточно нетерминалов!");
        }
        if (currentNonTerminal == 'S') currentNonTerminal++;
        return String.valueOf(currentNonTerminal++);
    }

    // 5. Эквивалентны ли грамматики с правилами:
    // 6. Построить КС-грамматику, эквивалентную грамматике с правилами:
    // 7. Построить регулярную грамматику, эквивалентную грамматике с правилами:
    // 9. Дана грамматика G:
    //S  aSbS | bSaS | ε
    //а) Постройте все возможные деревья вывода для цепочки abab
    // 11. Написать леволинейную регулярную грамматику, эквивалентную данной праволинейной,
    //допускающую детерминированный разбор
    // 12. Даны две грамматики G1 и G2, порождающие языки L1 и L2. Построить регулярную
    //грамматику для L1  L2. Для полученной грамматики построить детерминированный конечный
    //автомат

    private ArrayList<Rule> createRulesForStandardTerminals(HashMap<String, String> terminalsAndConstraints, String startNonTerminal) {
        ArrayList<Rule> rules = new ArrayList<>();
        String currentNonTerminal = startNonTerminal;

        for (Map.Entry<String, String> entry : terminalsAndConstraints.entrySet()) {
            String terminal = entry.getKey();
            String constraint = entry.getValue();

            // Разбираем ограничение
            String[] parts = constraint.split(" ");
            String constraintVar = parts[0];
            String operator = parts[1];
            String value = parts[2];
            if (operator.equals("=")) {
                // Фиксированное количество терминалов
                int count = Integer.parseInt(value);
                StringBuilder ruleValue = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    ruleValue.append(terminal).append("");
                }
                String nextNonTerminal = getNextNonTerminal();
                rules.add(new Rule(currentNonTerminal, ruleValue.toString().trim() + " " + nextNonTerminal));
                rules.add(new Rule(nextNonTerminal, "ε"));
                currentNonTerminal = nextNonTerminal;
            } else if (operator.equals(">") || operator.equals(">=")) {
                // Рекурсивное правило для n > 0 или n >= 1
                int minCount = Integer.parseInt(value);
                if (operator.equals(">")) {
                    minCount += 1;
                }

                StringBuilder ruleValue = new StringBuilder();
                for (int i = 0; i < minCount; i++) {
                    ruleValue.append(terminal).append("");
                }
                String nextNonTerminal = getNextNonTerminal();
                ruleValue.append(nextNonTerminal);
                rules.add(new Rule(currentNonTerminal, ruleValue.toString().trim()));

                rules.add(new Rule(nextNonTerminal, terminal + " " + nextNonTerminal + " | " + terminal));
                currentNonTerminal = nextNonTerminal;
            }
        }

        return rules;
    }

    public String convertToRegular(String grammar) {
        Map<String, ArrayList<String>> rules = new LinkedHashMap<>();
        parseGrammar(grammar, rules);

        StringBuilder regularGrammar = new StringBuilder();
        for (Map.Entry<String, ArrayList<String>> entry : rules.entrySet()) {
            String nonTerminal = entry.getKey();
            for (String production : entry.getValue()) {
                regularGrammar.append(nonTerminal).append(" = ").append(production).append("\n");
            }
        }
        return regularGrammar.toString();
    }


    private void parseGrammar(String grammar, Map<String, ArrayList<String>> rules) {
        Pattern pattern = Pattern.compile("([A-Z])\\s*=\\s*(.+)");
        for (String line : grammar.split("\n")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String nonTerminal = matcher.group(1);
                String[] productions = matcher.group(2).split("\\|");
                rules.computeIfAbsent(nonTerminal, k -> new ArrayList<>()).addAll(Arrays.asList(productions));
            }
        }
    }
}