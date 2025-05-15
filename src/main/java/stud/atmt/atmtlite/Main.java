package stud.atmt.atmtlite;

//a) Осуществить разбор цепочек 1011, 10+011 и 0–101+1.
//b) Восстановить регулярную грамматику, по которой была построена данная ДС.
//c) Какой язык порождает полученная грамматика?

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {

        System.out.println("Задание 2");
        System.out.println();

        NFA nfa = buildNFA();
        List<Rule> rules = nfa.buildRules();
        FormalLanguage formalLanguage = new FormalLanguage(rules, "S");
        String lng = formalLanguage.describe();
        String rgr = formalLanguage.buildRegular(Objects.requireNonNull(parseLanguage(lng)));

        System.out.println(nfa.accepts("1011x"));
        System.out.println(nfa.accepts("10+011x"));
        System.out.println(nfa.accepts("0-101+1x"));
        System.out.println();
        System.out.println(lng);
        System.out.println(rgr);
    }

    public static NFA buildNFA() {
        NFA nfa = new NFA();

        nfa.getStates().add("S");
        nfa.getStates().add("A");
        nfa.getStates().add("B");
        nfa.getStates().add("E");
        nfa.addAcceptingState("H");

        nfa.addTransition("S", '0', "A");
        nfa.addTransition("S", '1', "A");

        nfa.addEpsilonTransition("S", "E");
        nfa.addEpsilonTransition("A", "E");
        nfa.addEpsilonTransition("B", "E");

        nfa.addTransition("A", '0', "A");
        nfa.addTransition("A", '1', "A");

        nfa.addTransition("A", 'x', "H");

        nfa.addTransition("A",'+', "B");
        nfa.addTransition("A",'-', "B");
        nfa.addTransition("B",'0', "A");
        nfa.addTransition("B",'1', "A");

        return nfa;
    }

    public static GrammarApp.ParsedLanguage parseLanguage(String input) {
        if (isLanguageCorrect(input)) {
            HashMap<String, String> terminalsAndConstraints = getTerminals(input); // терминал - его ограничение a^n | n > 0 -> {a, n>0}
            HashMap<String, String> terminalsGroups = getGroups(input);
            HashMap<String, String> palindrome = getPalindrome(input);
            ArrayList<String> randomTerminals = getRandomTerminals(input); // описаны как <a,b,c,...> E (G)
            return new GrammarApp.ParsedLanguage(terminalsAndConstraints, terminalsGroups, palindrome, randomTerminals);
        }
        return null;
    }

    private static ArrayList<String> getRandomTerminals(String input) {
        ArrayList<String> randomTerminals = new ArrayList<>();

        // Ищем часть строки, которая соответствует < терминалы > E (G)
        Pattern pattern = Pattern.compile("\\<([^}]+)\\>\\s*E\\s*\\(G\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Извлекаем терминалы внутри фигурных скобок
            String terminalsPart = matcher.group(1).trim();
            String[] terminals = terminalsPart.split(",");

            for (String terminal : terminals) {
                terminal = terminal.trim();
                // Добавляем все терминалы, включая цифры
                randomTerminals.add(terminal);
            }
        }

        return randomTerminals;
    }

    private static HashMap<String, String> getPalindrome(String input) {
        HashMap<String, String> palindrome = new HashMap<>();

        // Разделяем входную строку на две части: до | и после |
        String[] parts = input.split("\\|");
        if (parts.length < 2) return palindrome; // Если нет ограничений, возвращаем пустой результат

        String terminalsPart = parts[0].trim(); // Часть до |
        String constraintsPart = parts[1].trim(); // Часть после |

        // Регулярное выражение для поиска палиндромов в двух форматах:
        // 1. [vars...]**<indexVar> (например, [g1,g2,g3]**i)
        // 2. [vars^indexVar,...]**<indexVar> (например, [t^j, r^j]**j)
        Pattern palindromePattern = Pattern.compile(
                "\\[(" +
                        "([a-z]+\\d,?)+" + // Формат 1: g1,g2,g3
                        "|" +
                        "([a-z]+\\^[a-z]+,?)+" + // Формат 2: t^j, r^j
                        ")\\]\\*\\*[a-z]"
        );
        Matcher palindromeMatcher = palindromePattern.matcher(terminalsPart);

        Pattern constraintPattern = Pattern.compile("([a-z0]\\*\\*[a-z]+)(\\s*=\\s*\\{.+\\})?");
        Matcher constraintMatcher = constraintPattern.matcher(constraintsPart);

        // Ищем палиндромы
        while (palindromeMatcher.find()) {
            String palindromeTerm = palindromeMatcher.group().trim(); // Найденный палиндром
            String palindromeVars = palindromeTerm.replaceAll("\\[|\\]\\*\\*[a-z]", ""); // Извлекаем переменные
            String indexVar = palindromeTerm.replaceAll(".*\\*\\*", ""); // Извлекаем indexVar

            // Ищем ограничения для этого палиндрома
            while (constraintMatcher.find()) {
                String constraintTerm = constraintMatcher.group().trim(); // Найденное ограничение
                String constraintVar = constraintTerm.replaceAll("\\*\\*.*", ""); // Извлекаем переменную
                String constraintIndexVar = constraintTerm.replaceAll(".*\\*\\*", "").replaceAll("\\s*=.*", ""); // Извлекаем indexVar

                // Если indexVar совпадает, добавляем ограничение
                if (indexVar.equals(constraintIndexVar)) {
                    String constraintValue = constraintTerm.replaceAll(".*=", "").trim(); // Извлекаем значение ограничения
                    palindrome.put(palindromeVars, constraintValue);
                    break;
                }
            }
        }

        return palindrome;
    }

    private static HashMap<String, String> getGroups(String input) {
        HashMap<String, String> terminalsGroups = new HashMap<>();
        // Разделяем входную строку на две части: до | и после |
        String[] parts = input.split("\\|");
        if (parts.length < 2) return terminalsGroups;

        String terminalsPart = parts[0].replaceAll("L\\s*=\\s*\\{", "").trim(); // Часть до |
        String constraintsPart = parts[1].trim(); // Часть после |

        // Извлекаем группы терминалов
        String[] terminals = terminalsPart.split(",");
        for (String terminal : terminals) {
            terminal = terminal.trim();
            // Ищем группы терминалов в формате (cdx)^n3, (fire)^n4 и т.д.
            if (terminal.matches("\\(.+\\)\\^[a-z0-9]+")) {
                String[] terminalParts = terminal.split("\\^");
                String group = terminalParts[0]; // Группа терминалов (например, (cdx))
                String constraintVar = terminalParts[1]; // Переменная ограничения (например, n3)

                // Ищем ограничение для этой переменной в части после |
                Pattern constraintPattern = Pattern.compile(constraintVar + "\\s*([<>=]+)\\s*(\\d+)");
                Matcher constraintMatcher = constraintPattern.matcher(constraintsPart);

                if (constraintMatcher.find()) {
                    String operator = constraintMatcher.group(1).trim(); // Оператор (например, >)
                    String value = constraintMatcher.group(2).trim(); // Значение (например, 0)
                    terminalsGroups.put(group, constraintVar + " " + operator + " " + value);
                }
            }
        }

        return terminalsGroups;
    }

    private static HashMap<String, String> getTerminals(String input) {
        HashMap<String, String> terminalsAndConstraints = new HashMap<>();
        // Разделяем входную строку на две части: до | и после |
        String[] parts = input.split("\\|");
        if (parts.length < 2) return terminalsAndConstraints;

        String terminalsPart = parts[0].replaceAll("L\\s*=\\s*\\{", "").trim(); // Часть до |
        String constraintsPart = parts[1].trim(); // Часть после |

        // Извлекаем терминалы
        String[] terminals = terminalsPart.split(",");
        for (String terminal : terminals) {
            terminal = terminal.trim();
            // Ищем терминалы в формате a^n1, b^n2 и т.д.
            if (terminal.matches(".+\\^[a-z0-9]+")) {
                String[] terminalParts = terminal.split("\\^");
                String symbol = terminalParts[0]; // Терминал (например, a)
                String constraintVar = terminalParts[1]; // Переменная ограничения (например, n1)

                // Ищем ограничение для этой переменной в части после |
                Pattern constraintPattern = Pattern.compile(constraintVar + "\\s*([<>=]+)\\s*(\\d+)");
                Matcher constraintMatcher = constraintPattern.matcher(constraintsPart);

                if (constraintMatcher.find()) {
                    String operator = constraintMatcher.group(1).trim(); // Оператор (например, >)
                    String value = constraintMatcher.group(2).trim(); // Значение (например, 0)
                    terminalsAndConstraints.put(symbol, constraintVar + " " + operator + " " + value);
                }
            }
        }

        return terminalsAndConstraints;
    }

    private static boolean isLanguageCorrect(String input) {
        // Проверяем, соответствует ли строка формату L = {терминалы | ограничения}
        return input.matches("L\\s*=\\s*\\{.*\\|.*}");
    }

    private String firstSymbol = "";

}
