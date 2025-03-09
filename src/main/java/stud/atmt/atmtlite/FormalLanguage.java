package stud.atmt.atmtlite;

import java.util.*;

public class FormalLanguage {
    private List<Rule> rules; // Список правил
    private int maxRepetitionsCount; // Максимальное количество повторений
    private Grammar grammar;
    private String firstSymbol;

    public FormalLanguage(List<Rule> rules, int maxRepetitionsCount, String firstSymbol) {
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
        createChainsAndDefineTerminals();
        result.append("L = { ").append(getTerminalPart()).append(getConstraintPart()).append(" }.");
        return result.toString();
    }

    ArrayList<String> serialTerminals = new ArrayList<>();
    HashMap<String, String> terminalsAndCounters = new HashMap<>();
    HashMap<String, Integer> minTerminalOccurrences = new HashMap<>();
    ArrayList<String> nonSerial = new ArrayList<>();
    HashMap<String, Integer> countableTerminals = new HashMap<>();

    private void createChainsAndDefineTerminals() {
        ArrayList<String> chains = generateChains(50);
        countTerminalsOccurrences(chains);
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

    private ArrayList<String> generateChains(int chains) {
        ArrayList<String> result = new ArrayList<>();
        HashSet<String> uniqueChains = new HashSet<>(); // Для хранения уникальных цепочек
        Random random = new Random();
        int maxIterations = 10000; // Максимальное количество итераций для одной цепочки
        int maxAttempts = chains * 10; // Максимальное число попыток генерации цепочки

        // Список всех правил для систематического использования
        List<Rule> allRules = new ArrayList<>(rules);
        int ruleIndex = 0; // Индекс для систематического выбора правил

        int attempts = 0;
        while (result.size() < chains && attempts < maxAttempts) {
            attempts++;
            System.out.println("Генерация цепочки " + (result.size() + 1) + " из " + chains);
            String currentChain = firstSymbol;
            boolean isTerminal = false;
            int iterations = 0;

            // Пытаемся получить цепочку, состоящую только из терминалов
            while (!isTerminal && iterations < maxIterations) {
                iterations++;
                isTerminal = true;
                StringBuilder newChain = new StringBuilder();

                for (char symbol : currentChain.toCharArray()) {
                    if (Character.isUpperCase(symbol)) { // Нетерминал
                        isTerminal = false;
                        List<Rule> applicableRules = new ArrayList<>();
                        for (Rule rule : rules) {
                            if (rule.getKey().equals(String.valueOf(symbol))) {
                                applicableRules.add(rule);
                            }
                        }
                        if (!applicableRules.isEmpty()) {
                            // Чередуем случайный выбор и систематический выбор правил
                            Rule selectedRule;
                            if (ruleIndex < allRules.size()) {
                                selectedRule = allRules.get(ruleIndex);
                                ruleIndex++;
                            } else {
                                selectedRule = applicableRules.get(random.nextInt(applicableRules.size()));
                            }
                            newChain.append(selectedRule.getValue());
                        } else {
                            newChain.append(symbol); // Если нет правил, оставляем символ как есть
                        }
                    } else { // Терминал
                        newChain.append(symbol);
                    }
                }
                currentChain = newChain.toString();
            }

            // Если цепочка не получилась терминальной, выводим сообщение и пропускаем её
            if (!isTerminal) {
                System.out.println("Не удалось получить цепочку, состоящую только из терминалов после " + maxIterations +
                        " итераций. Грамматика недостижима");
                // Можно выбрать прерывание генерации, если ни одна цепочка не завершается терминально:
                break;
            }

            // Проверяем уникальность цепочки
            if (!uniqueChains.contains(currentChain)) {
                uniqueChains.add(currentChain);
                result.add(currentChain);
            } else {
                System.out.println("Цепочка уже существует: " + currentChain + ". Пропуск.");
            }
        }

        if (result.isEmpty()) {
            System.out.println("Ни одной терминальной цепочки получено. Вероятно, язык недескриптируемый.");
        }
        return result;
    }

    private String getConstraintPart() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : terminalsAndCounters.entrySet()) {
            if (countableTerminals.containsKey(entry.getKey())) {
                result.append(entry.getValue()).append(" = ").append(countableTerminals.get(entry.getKey())).append("; ");
            } else {
                result.append(entry.getValue()).append(" > ").append(getMinOccurrencesNumb(entry.getKey())).append("; ");
            }
        }
        if (result.isEmpty()) return "";
        return " | " + result;
    }

    private int getMinOccurrencesNumb(String key) {
        return minTerminalOccurrences.get(key);
    }

    private String getTerminalPart() {
        return getTerminalSequence() + getTerminalArray();
    }

    private String getTerminalArray() {
        if (!nonSerial.isEmpty()) return " {" + getNonSerialTerminals() + "} " + "contains in (G); ";
        return "";
    }

    private String getNonSerialTerminals() {
        StringBuilder result = new StringBuilder();
        for (String term : nonSerial) {
            result.append(term).append("; ");
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
                result.append(term).append("^").append(count).append("; ");
                terminalsAndCounters.put(term, count);
            }
        } else {
            // Если не связаны, используем отдельные переменные для каждого терминала
            for (String term : serialTerminals) {
                String count = getCountForTerminal();
                result.append(term).append("^").append(count).append("; ");
                terminalsAndCounters.put(term, count);
            }
        }

        return result.toString();
    }
}
