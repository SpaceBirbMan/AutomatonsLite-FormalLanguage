package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrammarApp extends Application {

    // 3, 5, 6, 7, 9а, 11, 12

    @Override
    public void start(Stage primaryStage) {
        // Основные элементы интерфейса
        TextArea inputArea = new TextArea();
        TextArea languageArea = new TextArea();

        inputArea.setPromptText("Введите правила грамматики (например, S = aB | b)");
        languageArea.setPromptText("Введите язык в формате L = { a^n1, b^n2 | n1 > 0, n2 = 2 }");

        TextField targetInput = new TextField();
        targetInput.setPromptText("Введите целевую цепочку (например, aab)");

        Button describeLanguageButton = new Button("Описать язык");
        Button translateButton = new Button("Перевести");
        Button outputLeftButton = new Button("Левый вывод");
        Button typeGrammarButton = new Button("Тип грамматики");
        Button deriveButton = new Button("Путь к целевой цепочке");
        Button makeGrammarButton = new Button("Построить грамматику по языку");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        // Обработка кнопки "Перевести"
        translateButton.setOnAction(event -> {
            String input = inputArea.getText();
            List<Rule> rules = parseRules(input);
            FormalLanguage fl = new FormalLanguage(rules, firstSymbol);
            String result = fl.translate(firstSymbol);
            outputArea.setText("Результат перевода: " + result);
        });

        describeLanguageButton.setOnAction(actionEvent -> {
            String input = inputArea.getText();
            List<Rule> rules = parseRules(input);
            FormalLanguage formalLanguage = new FormalLanguage(rules, firstSymbol);
            String result = formalLanguage.describe();
            outputArea.setText("Описание: " + result);
        });

        // Обработка кнопки "Левый вывод"
        outputLeftButton.setOnAction(event -> {
            String input = inputArea.getText();
            List<Rule> rules = parseRules(input);
            FormalLanguage fl = new FormalLanguage(rules, firstSymbol);
            String result = fl.outputLeft();
            outputArea.setText("Левый вывод: " + result);
        });

        // Обработка кнопки "Тип грамматики"
        typeGrammarButton.setOnAction(event -> {
            String input = inputArea.getText();
            List<Rule> rules = parseRules(input);
            Grammar grammar = new Grammar(rules);
            grammar.setStartSymbol(firstSymbol);
            String result = grammar.getTypeGrammar();
            outputArea.setText("Тип грамматики: " + result);
        });

        // Обработка кнопки "Путь к целевой цепочке"
        // todo: Криво работает
        deriveButton.setOnAction(event -> {
            String input = inputArea.getText();
            String target = targetInput.getText();
            List<Rule> rules = parseRules(input);
            Grammar grammar = new Grammar(rules);
            grammar.setStartSymbol(firstSymbol);
            List<String> sequence = grammar.makeSequence(target);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < sequence.size()-1; i++) {
                result.append(sequence.get(i)).append(" -> ");
            }
            result.append(sequence.getLast());
            outputArea.setText(result.toString());
        });

        makeGrammarButton.setOnAction(event -> {
            String input = languageArea.getText();
            Grammar grammar = new Grammar();
            System.out.println(parseLanguage(input).toString());
            inputArea.setText(grammar.buildGrammar(parseLanguage(input)));
        });

        // Компоновка интерфейса
        VBox root = new VBox(10, inputArea, languageArea, targetInput, translateButton, outputLeftButton, makeGrammarButton, typeGrammarButton, deriveButton, describeLanguageButton, outputArea);
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Грамматический процессор");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public record ParsedLanguage(HashMap<String, String> terminalsAndConstraints,
                                 HashMap<String, String> terminalsGroups,
                                 HashMap<String, String> palindrome,
                                 ArrayList<String> randomTerminals) {}

    private ParsedLanguage parseLanguage(String input) {
        if (isLanguageCorrect(input)) {
            HashMap<String, String> terminalsAndConstraints = getTerminals(input); // терминал - его ограничение a^n | n > 0 -> {a, n>0}
            HashMap<String, String> terminalsGroups = getGroups(input);
            HashMap<String, String> palindrome = getPalindrome(input);
            ArrayList<String> randomTerminals = getRandomTerminals(input); // описаны как {a,b,c,...} E (G)
            return new ParsedLanguage(terminalsAndConstraints, terminalsGroups, palindrome, randomTerminals);
        }
        return null;
    }

    private ArrayList<String> getRandomTerminals(String input) {
        ArrayList<String> randomTerminals = new ArrayList<>();

        // Ищем часть строки, которая соответствует { терминалы } E (G)
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

    private HashMap<String, String> getPalindrome(String input) {
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

        // Регулярное выражение для поиска ограничений в формате <var>**<indexVar> = { ... }
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

    private HashMap<String, String> getGroups(String input) {
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

    private HashMap<String, String> getTerminals(String input) {
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
            if (terminal.matches("[a-z]+\\^[a-z0-9]+")) {
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

    private boolean isLanguageCorrect(String input) {
        // Проверяем, соответствует ли строка формату L = {терминалы | ограничения}
        return input.matches("L\\s*=\\s*\\{.*\\|.*}");
    }

    private String firstSymbol = "";

    // Парсинг правил из текста
    private List<Rule> parseRules(String input) {
        List<Rule> rules = new ArrayList<>();
        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.replaceAll("\\s+", ""); // Убираем пробелы
            if (line.contains("=")) {
                String[] parts = line.split("=");
                String leftSide = parts[0];
                if (firstSymbol.isEmpty()) {
                    firstSymbol = leftSide;
                }
                    String[] rightSides = parts[1].split("\\|");

                for (String rightSide : rightSides) {
                    rules.add(new Rule(leftSide, rightSide));
                }
            }
        }

        return rules;
    }

    public static void main(String[] args) {
        launch(args);
    }
}