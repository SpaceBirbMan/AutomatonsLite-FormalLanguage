package stud.atmt.atmtlite;

import java.util.*;

public class FormalLanguage {
    private List<Rule> rules; // Список правил
    private int maxRepetitionsCount; // Максимальное количество повторений
    private Grammar grammar;
    private String firstSymbol;

    public FormalLanguage(List<Rule> rules, int maxRepetitionsCount, String firstSymbol) {
        Bus.globalError = "";
        this.rules = rules;
        this.firstSymbol = firstSymbol;
        this.maxRepetitionsCount = maxRepetitionsCount;
        this.grammar = new Grammar(rules);
    }

    public FormalLanguage(List<Rule> rules, String firstSymbol) {
        this(rules, 10000, firstSymbol);
    }

    private boolean checkLoop(String input, Rule rule, int count) {
        for (int i = 0; i < count; i++) {
            int pos = input.indexOf(rule.getKey());
            if (pos != -1) {
                input = input.substring(0, pos) + rule.getValue() + input.substring(pos + rule.getKey().length());
            } else {
                return false;
            }
        }
        return true;
    }

    public String outputLeft() {
        String result = firstSymbol;
        int count = 0;
        while (count < maxRepetitionsCount) {
            int pos = -1;
            Rule selectedRule = null;
            for (Rule rule : rules) {
                int findPos = result.indexOf(rule.getKey());
                if (findPos != -1 && (pos == -1 || findPos < pos)) {
                    pos = findPos;
                    selectedRule = rule;
                }
            }
            if (pos == -1) break; // Нет подходящих правил
            result = result.substring(0, pos) + selectedRule.getValue() + result.substring(pos + selectedRule.getKey().length());
            count++;
        }
        return result;
    }

    public String translate(String text) {
        int count = 0;
        boolean isEnd = false;
        while (count < maxRepetitionsCount && !isEnd) {
            isEnd = true;
            for (Rule rule : rules) {
                if (!rule.isLooped()) {
                    int pos = text.indexOf(rule.getKey());
                    if (pos != -1) {
                        if (checkLoop(text, rule, 5)) {
                            rule.setLooped(true);
                        } else {
                            text = text.substring(0, pos) + rule.getValue() + text.substring(pos + rule.getKey().length());
                            isEnd = false;
                            break;
                        }
                    }
                } else {
                    rule.setLooped(false);
                }
            }
            count++;
        }
        return text;
    }

    public String describe() {
        StringBuilder result = new StringBuilder();
        extractTerminals();
        // todo.logic: Обработка палиндромов L = {a1a2…anan…a2a1 | a^i = {0, 1}}
        // todo.logic: Обработка групп терминалов {a^n,b^m,c^k}, d^n, e^n^2
        // todo.logic: Обработка групп терминалов (ab)^n (n раз последовательность ab)
        if (createChainsAndDefineTerminals()) {
            result.append("L = { ").append(getTerminalPart()).append(getConstraintPart()).append(" }");
            return result.toString();
        } else {
            return "Язык невозможно обработать данной программой из-за зацикленностей или слишком глубоких рекурсий";
        }
    }

    ArrayList<String> serialTerminals = new ArrayList<>();
    HashMap<String, String> terminalsAndCounters = new HashMap<>();
    HashMap<String, Integer> minTerminalOccurrences = new HashMap<>();
    ArrayList<String> nonSerial = new ArrayList<>();
    HashMap<String, Integer> countableTerminals = new HashMap<>();

    private boolean createChainsAndDefineTerminals() {
        ArrayList<String> chains = (ArrayList<String>) generateChains(50);
        if (chains == null) {
            return false;
        }
        countTerminalsOccurrences(chains);
        return true;
    }

    HashMap<String, ArrayList<Integer>> occMatrix;
    private void countTerminalsOccurrences(ArrayList<String> chains) {
        HashMap<String, Boolean> occChFlags = new HashMap<>(); // Менялось ли количество вхождений в разных цепочках
        HashMap<String, Integer> prevOccs = new HashMap<>();   // Данные о предыдущей цепочке
        occMatrix = new HashMap<>(); // Матрица количества вхождений терминалов

        for (String chain : chains) {
            HashMap<String, Integer> occ = new HashMap<>();   // Данные о текущей цепочке
            char[] symbols = chain.toCharArray();

            // Подсчёт вхождений терминалов в текущей цепочке
            for (char symbol : symbols) {
                String terminal = String.valueOf(symbol);
                occ.put(terminal, occ.getOrDefault(terminal, 0) + 1);
            }

            // Обновляем occMatrix для каждого терминала
            for (String terminal : occ.keySet()) {
                occMatrix.computeIfAbsent(terminal, k -> new ArrayList<>()).add(occ.get(terminal));
            }

            // Проверка на непрерывность последовательностей
            for (String terminal : occ.keySet()) {
                if (nonSerial.contains(terminal)) {
                    continue; // Если терминал уже в nonSerial, пропускаем
                }

                // Проверяем, образует ли терминал более одной последовательности
                if (hasMultipleSequences(chain, terminal)) {
                    nonSerial.add(terminal);
                    continue;
                }

                // Проверяем, изменилось ли количество вхождений терминала
                if (prevOccs.containsKey(terminal)) {
                    if (!prevOccs.get(terminal).equals(occ.get(terminal))) {
                        occChFlags.put(terminal, true); // Количество вхождений изменилось
                    }
                } else {
                    prevOccs.put(terminal, occ.get(terminal));
                }
            }
        }

        // Классификация терминалов
        for (String terminal : prevOccs.keySet()) {
            if (!nonSerial.contains(terminal)) {
                serialTerminals.add(terminal);
            }
            if (!occChFlags.getOrDefault(terminal, false)) {
                countableTerminals.put(terminal, prevOccs.get(terminal));
            }
        }

        // Заполняем minTerminalOccurrences минимальными значениями из occMatrix
        for (String terminal : occMatrix.keySet()) {
            ArrayList<Integer> occurrences = occMatrix.get(terminal);
            int minOccurrences = Collections.min(occurrences);
            minTerminalOccurrences.put(terminal, minOccurrences);
        }
    }

    // Проверка, образует ли терминал более одной непрерывной последовательности
    private boolean hasMultipleSequences(String chain, String terminal) {
        int count = 0;
        boolean inSequence = false;

        for (char symbol : chain.toCharArray()) {
            if (String.valueOf(symbol).equals(terminal)) {
                if (!inSequence) {
                    count++;
                    inSequence = true;
                }
            } else {
                inSequence = false;
            }

            if (count > 1) {
                return true; // Более одной последовательности
            }
        }

        return false; // Одна или ноль последовательностей
    }

    // todo.bug: Криво определяется первый символ, из-за чего странно работают алгоритмы, первое правило должно выносится в отдельную переменную и к общим не лезть, возможно
    // todo.tips: Переписать всё на английский

    private HashSet<String> terminals = new HashSet<>();

    public HashSet<String> getTerminals() {
        return terminals;
    }

    public void setTerminals(HashSet<String> terminals) {
        this.terminals = terminals;
    }

    private void extractTerminals() {
        for (Rule rule : rules) {
            char[] chars;
            chars = rule.getKey().toCharArray();
            checkCharArray(chars);
            chars = rule.getValue().toCharArray();
            checkCharArray(chars);
        }
    }

    /// Add terminals from char array in set automatically
    private void checkCharArray(char[] chars) {
        for (char ch : chars) {
            String strChar = String.valueOf(ch);
            String lowerStr = strChar.toLowerCase();
            if (strChar.equals(lowerStr)) {
                terminals.add(strChar);
            }
        }
    }

    public String buildRegular(GrammarApp.ParsedLanguage language) {
        StringBuilder grammar = new StringBuilder();

        Set<String> terminals = new HashSet<>(language.randomTerminals());
        Map<String, String> constraints = language.terminalsAndConstraints();
        List<String> fixedSymbols = new ArrayList<>(constraints.keySet());

        // Генерируем имена нетерминалов: S, A, B, C, ...
        List<String> nonTerminals = new ArrayList<>();
        nonTerminals.add("S"); // Первый нетерминал - S
        for (int i = 0; i < fixedSymbols.size(); i++) {
            nonTerminals.add(Character.toString((char) ('A' + i)));
        }

        grammar.append(nonTerminals.get(0)).append(" = ");
        for (String term : terminals) {
            grammar.append(term).append(nonTerminals.get(0)).append(" | ");
        }
        if (!fixedSymbols.isEmpty()) {
            grammar.append(fixedSymbols.get(0)).append(" ").append(nonTerminals.get(1)).append(" | ");
        }
        grammar.append("ε\n");

        // Правила для фиксированных символов
        for (int i = 0; i < fixedSymbols.size(); i++) {
            grammar.append(nonTerminals.get(i + 1)).append(" = ");
            if (i + 1 < fixedSymbols.size()) {
                grammar.append(fixedSymbols.get(i + 1)).append(" ").append(nonTerminals.get(i + 2)).append(" | ");
            }
            // Добавляем терминалы после фикс. символов
            for (String term : terminals) {
                grammar.append(term).append(nonTerminals.get(i + 1)).append(" | ");
            }
            grammar.append("ε\n");
        }

        return grammar.toString();
    }

    public String buildLeft(boolean optimization) {
        List<Rule> newRules = swapToLeft(rebuildRecursion(optimizeForDet(rules)));
        System.out.println("Результат" + newRules);
        if (isLL1(swapToLeft(newRules))) {
            StringBuilder result = new StringBuilder();
            Grammar grammar1 = new Grammar();
            if (optimization) newRules = grammar1.optimizeRules(newRules);
            for (Rule rule : newRules) {
                result.append(rule.getKey()).append(" = ").append(rule.getValue()).append("\n");
            }
            return result.toString();
        }
        return "Ошибка обработки";
    }

    private List<Rule> swapToLeft(List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        Set<String> nonTerminals = new HashSet<>();
        Set<String> terminals = new HashSet<>();

        // 1. Анализ существующих символов
        for (Rule rule : rules) {
            nonTerminals.add(rule.getKey());
            for (char c : rule.getValue().toCharArray()) {
                String s = String.valueOf(c);
                if (!s.equals("ε") && !Character.isUpperCase(c)) {
                    terminals.add(s);
                }
            }
        }

        // 2. Преобразование каждого правила
        for (Rule rule : rules) {
            String rhs = rule.getValue();

            // Пропускаем ε-правила
            if (rhs.equals("ε")) {
                result.add(rule);
                continue;
            }

            // Разбиваем на символы, исключая апострофы
            List<String> symbols = new ArrayList<>();
            for (char c : rhs.toCharArray()) {
                String symbol = String.valueOf(c);
                if (!symbol.equals("'")) {
                    symbols.add(symbol);
                }
            }

            // Проверяем и преобразуем в леволинейную форму
            if (isValidLeftLinear(String.valueOf(symbols))) {
                result.add(rule);
            } else {
                // Поэтапное преобразование
                String currentNT = rule.getKey();
                String remaining = rhs.replace("'", ""); // Удаляем апострофы

                while (!isValidLeftLinear(remaining)) {
                    // Находим первый терминал не в конце
                    int termPos = findTerminalNotLast(remaining);
                    if (termPos == -1) break;

                    String newNT = generateNewNonTerminal(currentNT, nonTerminals);
                    nonTerminals.add(newNT);

                    char term = remaining.charAt(termPos);
                    String prefix = remaining.substring(0, termPos);
                    String suffix = remaining.substring(termPos + 1);

                    // Создаем новое правило
                    result.add(new Rule(currentNT, newNT + suffix.charAt(suffix.length() - 1)));

                    // Подготавливаем следующую итерацию
                    currentNT = newNT;
                    remaining = prefix + term;
                    if (suffix.length() > 1) {
                        remaining += suffix.substring(0, suffix.length() - 1);
                    }
                }

                // Добавляем последнее правило
                if (!remaining.isEmpty()) {
                    result.add(new Rule(currentNT, remaining));
                }
            }
        }

        return result;
    }

    private boolean isValidLeftLinear(String symbols) {
        if (symbols.isEmpty()) return false;

        // Один терминал — допустимо
        if (symbols.length() == 1) {
            return isTerminal(symbols);
        }

        // Несколько символов: первый — нетерминал, последний — терминал
        char first = symbols.charAt(0);
        char last = symbols.charAt(symbols.length() - 1);

        return Character.isUpperCase(first) && isTerminal(String.valueOf(last));
    }


    private int findTerminalNotLast(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            if (isTerminal(String.valueOf(s.charAt(i)))) {
                return i;
            }
        }
        return -1;
    }

    private String generateNewNonTerminal(String base, Set<String> existing) {
        int counter = 1;
        String newNT = base + "X";
        while (existing.contains(newNT)) {
            newNT = base + "X" + counter++;
        }
        return newNT;
    }

    private boolean isTerminal(String s) {
        return s.length() == 1 && !Character.isUpperCase(s.charAt(0));
    }


    private List<Rule> rebuildRecursion(List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        Map<String, List<Rule>> ruleMap = new HashMap<>();

        // Группируем правила по левой части
        for (Rule rule : rules) {
            ruleMap.computeIfAbsent(rule.getKey(), k -> new ArrayList<>()).add(rule);
        }

        // Обрабатываем каждый нетерминал
        for (String nonTerminal : new ArrayList<>(ruleMap.keySet())) {
            List<Rule> currentRules = ruleMap.get(nonTerminal);
            List<Rule> recursiveRules = new ArrayList<>();
            List<Rule> nonRecursiveRules = new ArrayList<>();

            // Разделяем правила на рекурсивные и нерекурсивные
            for (Rule rule : currentRules) {
                if (isRecursive(rule, nonTerminal)) {
                    recursiveRules.add(rule);
                } else {
                    nonRecursiveRules.add(rule);
                }
            }

            if (recursiveRules.isEmpty()) {
                // Нет рекурсии - оставляем как есть
                result.addAll(currentRules);
                continue;
            }

            // Создаем новый нетерминал для итерации
            String newNonTerminal = nonTerminal + "'";

            // 1. Добавляем нерекурсивные правила
            for (Rule rule : nonRecursiveRules) {
                // Если есть ε-правило, оставляем его, иначе добавляем переход к новому нетерминалу
                if (rule.getValue().equals("ε")) {
                    result.add(rule);
                } else {
                    result.add(new Rule(nonTerminal, rule.getValue() + newNonTerminal));
                }
            }

            // Если не было нерекурсивных правил, добавляем переход к новому нетерминалу
            if (nonRecursiveRules.isEmpty()) {
                result.add(new Rule(nonTerminal, newNonTerminal));
            }

            // 2. Обрабатываем рекурсивные правила
            for (Rule rule : recursiveRules) {
                String rhs = rule.getValue();

                // Удаляем рекурсивный вызов
                String newRhs = rhs.replace(nonTerminal, "");

                if (newRhs.isEmpty()) {
                    newRhs = "ε";
                }

                // Добавляем новое правило с новым нетерминалом
                result.add(new Rule(newNonTerminal, newRhs + newNonTerminal));
            }

            // Добавляем завершающее ε-правило
            result.add(new Rule(newNonTerminal, "ε"));
        }
        System.out.println(result);
        return result;
    }

    private boolean isRecursive(Rule rule, String nonTerminal) {
        String rhs = rule.getValue();
        return rhs.contains(nonTerminal);
    }

    public List<Rule> optimizeForDet(List<Rule> rules) {
        List<Rule> optimizedRules = new ArrayList<>();
        System.out.println(rules);
        // 1. Удаление левой рекурсии
        rules = eliminateLeftRecursion(rules);
        System.out.println(rules);
        // 2. Левая факторизация
        rules = leftFactor(rules);
        System.out.println(rules);
        // 3. Удаление бесполезных символов
        //rules = removeUselessSymbols(rules);
        System.out.println(rules);
        // 4. Оптимизация правил
        Map<String, List<String>> ruleMap = new HashMap<>();
        for (Rule rule : rules) {
            ruleMap.computeIfAbsent(rule.getKey(), k -> new ArrayList<>()).add(rule.getValue());
        }

        for (Map.Entry<String, List<String>> entry : ruleMap.entrySet()) {
            String nonTerminal = entry.getKey();
            List<String> productions = entry.getValue();

            // Группировка по первому символу
            Map<Character, List<String>> firstCharMap = new HashMap<>();
            for (String prod : productions) {
                char firstChar = prod.isEmpty() ? 'ε' : prod.charAt(0);
                firstCharMap.computeIfAbsent(firstChar, k -> new ArrayList<>()).add(prod);
            }

            // Создание новых правил
            for (Map.Entry<Character, List<String>> fcEntry : firstCharMap.entrySet()) {
                if (fcEntry.getValue().size() == 1) {
                    // Нет конфликтов - оставляем как есть
                    optimizedRules.add(new Rule(nonTerminal, fcEntry.getValue().get(0)));
                } else {
                    // Конфликт - вводим новый нетерминал
                    String newNonTerminal = nonTerminal + "'";
                    String commonPrefix = findLongestCommonPrefix(fcEntry.getValue());

                    optimizedRules.add(new Rule(nonTerminal, commonPrefix + newNonTerminal));

                    // Добавляем новые правила для суффиксов
                    for (String prod : fcEntry.getValue()) {
                        String suffix = prod.substring(commonPrefix.length());
                        optimizedRules.add(new Rule(newNonTerminal, suffix.isEmpty() ? "ε" : suffix));
                    }
                }
            }
        }
        System.out.println(optimizedRules);
        if (isLL1(optimizedRules)) return optimizedRules;
        return null;
    }

    public boolean isLL1(List<Rule> optimizedRules) {
        // 1. Вычисляем FIRST и FOLLOW множества
        Map<String, Set<Character>> firstSets = computeFirstSets(optimizedRules);
        Map<String, Set<Character>> followSets = computeFollowSets(optimizedRules, firstSets);

        // 2. Группируем правила по левой части
        Map<String, List<Rule>> ruleMap = new HashMap<>();
        for (Rule rule : optimizedRules) {
            ruleMap.computeIfAbsent(rule.getKey(), k -> new ArrayList<>()).add(rule);
        }

        // 3. Проверяем условия LL(1) для каждого нетерминала
        for (String nonTerminal : ruleMap.keySet()) {
            List<Rule> rulesForNT = ruleMap.get(nonTerminal);
            if (rulesForNT == null || rulesForNT.size() < 2) continue;

            // Получаем FOLLOW множество (может быть пустым, но не null)
            Set<Character> follow = followSets.getOrDefault(nonTerminal, Collections.emptySet());

            for (int i = 0; i < rulesForNT.size(); i++) {
                for (int j = i + 1; j < rulesForNT.size(); j++) {
                    Rule rule1 = rulesForNT.get(i);
                    Rule rule2 = rulesForNT.get(j);

                    Set<Character> first1 = computeFirst(rule1.getValue(), firstSets);
                    Set<Character> first2 = computeFirst(rule2.getValue(), firstSets);

                    // Проверка 1: FIRST(rule1) ∩ FIRST(rule2) ≠ ∅
                    if (setsIntersect(first1, first2)) {
                        return false;
                    }

                    // Проверка 2: Если ε ∈ FIRST(rule1), то FIRST(rule2) ∩ FOLLOW(A) ≠ ∅
                    if (first1.contains('ε') && setsIntersect(first2, follow)) {
                        return false;
                    }

                    // Проверка 3: Если ε ∈ FIRST(rule2), то FIRST(rule1) ∩ FOLLOW(A) ≠ ∅
                    if (first2.contains('ε') && setsIntersect(first1, follow)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Проверяет пересечение двух множеств
    private boolean setsIntersect(Set<Character> set1, Set<Character> set2) {
        if (set1 == null || set2 == null) return false;
        for (Character c : set1) {
            if (set2.contains(c)) return true;
        }
        return false;
    }

    private Map<String, Set<Character>> computeFirstSets(List<Rule> rules) {
        Map<String, Set<Character>> firstSets = new HashMap<>();
        boolean changed;

        // Инициализация
        for (Rule rule : rules) {
            firstSets.putIfAbsent(rule.getKey(), new HashSet<>());
        }

        do {
            changed = false;
            for (Rule rule : rules) {
                String lhs = rule.getKey();
                String rhs = rule.getValue();

                Set<Character> first = computeFirst(rhs, firstSets);
                Set<Character> currentFirst = firstSets.get(lhs);

                int oldSize = currentFirst.size();
                currentFirst.addAll(first);
                if (currentFirst.size() > oldSize) {
                    changed = true;
                }
            }
        } while (changed);

        return firstSets;
    }

    private Set<Character> computeFirst(String symbol, Map<String, Set<Character>> firstSets) {
        Set<Character> result = new HashSet<>();

        if (symbol.isEmpty() || symbol.equals("ε")) {
            result.add('ε');
            return result;
        }

        char firstChar = symbol.charAt(0);

        if (Character.isUpperCase(firstChar)) {
            // Нетерминал
            Set<Character> firstOfNT = firstSets.get(String.valueOf(firstChar));
            if (firstOfNT != null) {
                result.addAll(firstOfNT);
            }
        } else {
            // Терминал
            result.add(firstChar);
        }

        // Если первый символ может быть ε, проверяем следующий
        if (result.contains('ε') && symbol.length() > 1) {
            result.remove('ε');
            result.addAll(computeFirst(symbol.substring(1), firstSets));
        }

        return result;
    }

    private Map<String, Set<Character>> computeFollowSets(List<Rule> rules,
                                                          Map<String, Set<Character>> firstSets) {
        Map<String, Set<Character>> followSets = new HashMap<>();
        boolean changed;

        // 1. Инициализация: собираем ВСЕ нетерминалы (из левых и правых частей)
        Set<String> allNonTerminals = new HashSet<>();
        for (Rule rule : rules) {
            allNonTerminals.add(rule.getKey());
            // Добавляем нетерминалы из правой части
            for (char c : rule.getValue().toCharArray()) {
                if (Character.isUpperCase(c)) {
                    allNonTerminals.add(String.valueOf(c));
                }
            }
        }

        // 2. Инициализация followSets для всех нетерминалов
        for (String nt : allNonTerminals) {
            followSets.put(nt, new HashSet<>());
        }
        followSets.get("S").add('$'); // $ - символ конца строки для стартового нетерминала

        // 3. Вычисление follow-множеств
        do {
            changed = false;
            for (Rule rule : rules) {
                String rhs = rule.getValue();
                String lhs = rule.getKey();

                for (int i = 0; i < rhs.length(); i++) {
                    char c = rhs.charAt(i);
                    if (Character.isUpperCase(c)) {
                        String currentNt = String.valueOf(c);
                        Set<Character> currentFollow = followSets.get(currentNt);
                        if (currentFollow == null) {
                            currentFollow = new HashSet<>();
                            followSets.put(currentNt, currentFollow);
                        }

                        int oldSize = currentFollow.size();

                        if (i < rhs.length() - 1) {
                            String remaining = rhs.substring(i + 1);
                            Set<Character> firstOfRemaining = computeFirst(remaining, firstSets);

                            // Добавляем FIRST(remaining) кроме ε
                            for (Character ch : firstOfRemaining) {
                                if (ch != 'ε') {
                                    currentFollow.add(ch);
                                }
                            }

                            // Если FIRST(remaining) содержит ε, добавляем FOLLOW(lhs)
                            if (firstOfRemaining.contains('ε')) {
                                Set<Character> lhsFollow = followSets.get(lhs);
                                if (lhsFollow != null) {
                                    currentFollow.addAll(lhsFollow);
                                }
                            }
                        } else {
                            // Если нетерминал в конце, добавляем FOLLOW(lhs)
                            Set<Character> lhsFollow = followSets.get(lhs);
                            if (lhsFollow != null) {
                                currentFollow.addAll(lhsFollow);
                            }
                        }

                        if (currentFollow.size() > oldSize) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        return followSets;
    }

    private List<Rule> eliminateLeftRecursion(List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        Map<String, List<String>> ruleMap = new HashMap<>();

        for (Rule rule : rules) {
            ruleMap.computeIfAbsent(rule.getKey(), k -> new ArrayList<>()).add(rule.getValue());
        }

        for (String nonTerminal : new ArrayList<>(ruleMap.keySet())) {
            List<String> alphas = new ArrayList<>();
            List<String> betas = new ArrayList<>();

            for (String prod : ruleMap.get(nonTerminal)) {
                if (prod.startsWith(nonTerminal)) {
                    alphas.add(prod.substring(nonTerminal.length()));
                } else {
                    betas.add(prod);
                }
            }

            if (!alphas.isEmpty()) {
                String newNonTerminal = nonTerminal + "'";

                for (String beta : betas) {
                    result.add(new Rule(nonTerminal, beta + newNonTerminal));
                }

                for (String alpha : alphas) {
                    result.add(new Rule(newNonTerminal, alpha + newNonTerminal));
                }

                result.add(new Rule(newNonTerminal, "ε"));
            } else {
                for (String prod : ruleMap.get(nonTerminal)) {
                    result.add(new Rule(nonTerminal, prod));
                }
            }
        }

        return result;
    }

    private String getCommonPrefix(List<String> productions, String currentProd) {
        String longestPrefix = "";

        for (String prod : productions) {
            if (!prod.equals(currentProd)) {
                // Находим общий префикс между currentProd и prod
                int minLength = Math.min(currentProd.length(), prod.length());
                int commonLength = 0;

                while (commonLength < minLength
                        && currentProd.charAt(commonLength) == prod.charAt(commonLength)) {
                    commonLength++;
                }

                String currentPrefix = currentProd.substring(0, commonLength);

                // Обновляем самый длинный найденный префикс
                if (currentPrefix.length() > longestPrefix.length()) {
                    longestPrefix = currentPrefix;
                }
            }
        }

        return longestPrefix;
    }

    private List<Rule> leftFactor(List<Rule> rules) {
        // Реализация левой факторизации
        List<Rule> result = new ArrayList<>();
        Map<String, List<String>> ruleMap = new HashMap<>();

        for (Rule rule : rules) {
            ruleMap.computeIfAbsent(rule.getKey(), k -> new ArrayList<>()).add(rule.getValue());
        }

        for (String nonTerminal : ruleMap.keySet()) {
            List<String> productions = ruleMap.get(nonTerminal);
            Map<String, List<String>> prefixMap = new HashMap<>();

            for (String prod : productions) {
                String prefix = getCommonPrefix(productions, prod);
                prefixMap.computeIfAbsent(prefix, k -> new ArrayList<>()).add(prod);
            }

            for (Map.Entry<String, List<String>> entry : prefixMap.entrySet()) {
                if (entry.getValue().size() == 1) {
                    result.add(new Rule(nonTerminal, entry.getKey()));
                } else {
                    String newNonTerminal = nonTerminal + "'";
                    String commonPrefix = entry.getKey();

                    result.add(new Rule(nonTerminal, commonPrefix + newNonTerminal));

                    for (String prod : entry.getValue()) {
                        String suffix = prod.substring(commonPrefix.length());
                        result.add(new Rule(newNonTerminal, suffix.isEmpty() ? "ε" : suffix));
                    }
                }
            }
        }

        return result;
    }

    private List<Rule> removeUselessSymbols(List<Rule> rules) {
        // Реализация удаления бесполезных символов
        Set<String> generating = new HashSet<>();
        Set<String> reachable = new HashSet<>();

        // Шаг 1: Нахождение порождающих символов
        boolean changed;
        do {
            changed = false;
            for (Rule rule : rules) {
                if (isGenerating(rule.getValue(), generating) && !generating.contains(rule.getKey())) {
                    generating.add(rule.getKey());
                    changed = true;
                }
            }
        } while (changed);

        // Шаг 2: Нахождение достижимых символов
        reachable.add("S"); // Стартовый символ
        do {
            changed = false;
            for (Rule rule : rules) {
                if (reachable.contains(rule.getKey())) {
                    for (char c : rule.getValue().toCharArray()) {
                        String symbol = String.valueOf(c);
                        if (Character.isUpperCase(c) && !reachable.contains(symbol)
                                && generating.contains(symbol)) {
                            reachable.add(symbol);
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        // Фильтрация правил
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules) {
            if (reachable.contains(rule.getKey()) &&
                    allSymbolsGenerating(rule.getValue(), generating)) {
                result.add(rule);
            }
        }

        return result;
    }

    private boolean isGenerating(String production, Set<String> generating) {
        for (char c : production.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!generating.contains(String.valueOf(c))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean allSymbolsGenerating(String production, Set<String> generating) {
        for (char c : production.toCharArray()) {
            if (Character.isUpperCase(c) && !generating.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    private String findLongestCommonPrefix(List<String> strings) {
        if (strings.isEmpty()) return "";

        String prefix = strings.getFirst();
        for (String str : strings) {
            while (!str.startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        return prefix;
    }

    private class AddressChainCouple {
        private String chain;
        private boolean deadEnd;
        
        public AddressChainCouple(String chain, boolean isDeadEnd) {
            this.deadEnd = isDeadEnd;
            this.chain = chain;
        }

        public boolean isDeadEnd() {
            return deadEnd;
        }

        public void setDeadEnd(boolean deadend) {
            this.deadEnd = deadend;
        }

        public String getChain() {
            return chain;
        }
    }

    private final Set<String> deadEnds = new HashSet<>(); // Множество тупиковых цепочек

    /**
     * Генерация цепочек
     * @param chains количество цепочек, которые нужно сгенерировать
     * @return список тупиковых цепочек
     */
    public List<String> generateChains(long chains) {
        deadEnds.clear();
        NTree<AddressChainCouple> chainsGraph = new NTree<>(new AddressChainCouple(firstSymbol, isRootDeadEnd()));
        long iteration = 1;
        short repeats = 0;
        long iterationOld = 0;

        while (iteration < chains) {
            if (repeats > 5) break;
            iterationOld = iteration;
            // Получаем все тупиковые узлы на текущем этапе
            List<NJoint<AddressChainCouple>> leafNodes = chainsGraph.getLeafNodes();

            for (NJoint<AddressChainCouple> joint : leafNodes) {
                if (!joint.data.isDeadEnd()) {
                    // Генерируем возможные продолжения цепочки
                    List<String> ways = getWays(joint.data.getChain(), 10, new HashSet<>());

                    if (!ways.isEmpty()) {
                        // Добавляем новые цепочки в дерево
                        for (String way : ways) {
                            joint.AddLink(new NJoint<>(new AddressChainCouple(way, isDeadEnd(way))));
                            iteration++;
                        }
                    } else {
                        // Если продолжений нет, отмечаем узел как тупиковый
                        joint.data.setDeadEnd(true);
                        deadEnds.add(joint.data.getChain());
                    }
                }
            }
            if (iterationOld == iteration) {
                repeats++;
            }
        }

        // Собираем все тупиковые цепочки
        List<String> result = new ArrayList<>();
        for (NJoint<AddressChainCouple> joint : chainsGraph.getLeafNodes()) {
            if (joint.data.isDeadEnd()) {
                result.add(joint.data.getChain());
            }
        }

        // Проверяем результат
        if (checkResult(result)) {
            return result;
        }
        return null;
    }

    /**
     * Проверка результата
     * @param result список тупиковых цепочек
     * @return true, если результат корректный
     */
    private boolean checkResult(List<String> result) {
        if (result.isEmpty()) {
            return false; // Если результат пуст, генерация не удалась
        }
        return true;
    }

    /**
     * Проверка, является ли цепочка тупиковой
     * @param chain цепочка для проверки
     * @return true, если цепочка тупиковая
     */
    private boolean isDeadEnd(String chain) {
        // Если цепочка состоит только из терминалов, она тупиковая
        for (char c : chain.toCharArray()) {
            if (Character.isUpperCase(c)) { // Предполагаем, что нетерминалы — это заглавные буквы
                return false;
            }
        }
        return true;
    }

    /**
     * Получение всех возможных продолжений цепочки
     * @param chain текущая цепочка
     * @return список возможных продолжений
     */
    private List<String> getWays(String chain, int maxDepth, Set<String> visited) {
        if (maxDepth <= 0 || visited.contains(chain)) {
            return new ArrayList<>();
        }
        visited.add(chain);

        List<String> ways = new ArrayList<>();
        for (int i = 0; i < chain.length(); i++) {
            char symbol = chain.charAt(i);
            if (Character.isUpperCase(symbol)) {
                for (Rule rule : rules) {
                    if (rule.getKey().equals(String.valueOf(symbol))) {
                        String newChain = chain.substring(0, i) + rule.getValue() + chain.substring(i + 1);
                        ways.add(newChain);
                        ways.addAll(getWays(newChain, maxDepth - 1, visited));
                    }
                }
            }
        }
        return ways;
    }

    /**
     * Проверка, является ли корневой символ тупиковым
     * @return true, если корневой символ тупиковый
     */
    private boolean isRootDeadEnd() {
        for (Rule rule : rules) {
            if (rule.getKey().equals(firstSymbol)) {
                return rule.getValue() == null; // Если у корневого символа нет правил, он тупиковый
            }
        }
        return false;
    }

    private String getConstraintPart() {
        StringBuilder result = new StringBuilder();
        Set<String> uniqueConstraints = new HashSet<>(); // Для хранения уникальных условий

        for (Map.Entry<String, String> entry : terminalsAndCounters.entrySet()) {
            String constraint;
            if (countableTerminals.containsKey(entry.getKey())) {
                constraint = entry.getValue() + " = " + countableTerminals.get(entry.getKey());
            } else {
                constraint = entry.getValue() + " >= " + getMinOccurrencesNumb(entry.getKey());
            }

            // Добавляем условие только если оно уникально
            if (uniqueConstraints.add(constraint)) {
                result.append(constraint).append(", ");
            }
        }

        // Удаляем последнюю запятую, если строка не пустая
        if (result.length() > 0) {
            result.setLength(result.length() - 2); // Удаляем ", "
        }

        // Возвращаем результат с добавлением " | ", если строка не пустая
        return result.isEmpty() ? "" : " | " + result.toString();
    }

    private int getMinOccurrencesNumb(String key) {
        return minTerminalOccurrences.get(key);
    }

    private String getTerminalPart() {
        String terminalSequence = getTerminalSequence();
        String terminalArray = getTerminalArray();
        if (terminalArray.isEmpty()) {
            return terminalSequence;
        }
        if (terminalSequence.isEmpty()) {
            return terminalArray;
        }
        return getTerminalSequence() + ", " + getTerminalArray();
    }

    private String getTerminalArray() {
        if (!nonSerial.isEmpty()) return "<" + getNonSerialTerminals() + "> " + "E (G) ";
        return "";
    }

    private String getNonSerialTerminals() {
        StringBuilder result = new StringBuilder();
        for (String term : nonSerial) {
            result.append(term).append(", ");
        }
        // Удаляем последнюю запятую, если строка не пустая
        if (!result.isEmpty()) {
            result.setLength(result.length() - 2); // Удаляем ", "
        }
        return result.toString();
    }

    int counters = 0;

    private String getCountForTerminal() {
        return "n" + counters++;
    }

    private boolean areTerminalsCorrelated(HashMap<String, ArrayList<Integer>> occMatrix) {
        if (occMatrix.isEmpty()) {
            return false;
        }

        // Получаем список терминалов
        List<String> terminals = new ArrayList<>(occMatrix.keySet());

        // Проверяем, что количество вхождений всех терминалов изменяется пропорционально
        ArrayList<Integer> firstTerminalOccurrences = occMatrix.get(terminals.getFirst());
        for (int i = 1; i < terminals.size(); i++) {
            ArrayList<Integer> currentTerminalOccurrences = occMatrix.get(terminals.get(i));
            if (currentTerminalOccurrences.size() != firstTerminalOccurrences.size()) {
                return false;
            }

            // Проверяем, что соотношение вхождений одинаково для всех цепочек
            double ratio = (double) currentTerminalOccurrences.getFirst() / firstTerminalOccurrences.getFirst();
            for (int j = 1; j < firstTerminalOccurrences.size(); j++) {
                double currentRatio = (double) currentTerminalOccurrences.get(j) / firstTerminalOccurrences.get(j);
                if (Math.abs(currentRatio - ratio) > 0.0001) { // Учитываем погрешность
                    return false;
                }
            }
        }

        return true;
    }

    private String getTerminalSequence() {
        StringBuilder result = new StringBuilder();

        // Проверяем, связаны ли терминалы одной переменной
        if (areTerminalsCorrelated(occMatrix)) {
            // Если связаны, используем одну переменную n
            String count = getCountForTerminal();
            for (String term : serialTerminals) {
                result.append(term).append("^").append(count).append(", ");
                terminalsAndCounters.put(term, count);
            }
        } else {
            // Если не связаны, используем отдельные переменные для каждого терминала
            for (String term : serialTerminals) {
                String count = getCountForTerminal();
                result.append(term).append("^").append(count).append(", ");
                terminalsAndCounters.put(term, count);
            }
        }

        // Удаляем последнюю запятую, если строка не пустая
        if (!result.isEmpty()) {
            result.setLength(result.length() - 2); // Удаляем ", "
        }

        return result.toString();
    }
}
