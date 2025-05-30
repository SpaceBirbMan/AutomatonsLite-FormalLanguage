package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrammarApp extends Application {

    /*
    9. Дана грамматика G:
    S = aSbS | bSaS | ε
    а) Постройте все возможные деревья вывода для цепочки abab

    11. Написать леволинейную регулярную грамматику, эквивалентную данной праволинейной,
    допускающую детерминированный разбор:
    a)
    S = 0S | 0B
    B = 1B | 1C
    C = 1C | 
    б)
    S = aA | aB | bA
    A = bS
    B = aS | bB | 

    12. Даны две грамматики G1 и G2, порождающие языки L1 и L2. Построить регулярную
    грамматику для L1 v L2. Для полученной грамматики построить детерминированный конечный
    автомат.
    G1:
    S = S1 | A0
    A = A1 | 0A

    G2:
    S = A1 | B0 | E1
    A = S1
    B = C1 | D1
    C = 0
    D = B1
    E = E0 | 1
     */

    //todo.todo: Закоммитить

    @Override
    public void start(Stage primaryStage) {
        // Основные элементы интерфейса
        TextArea inputArea = new TextArea();
        TextArea languageArea = new TextArea();

        inputArea.setPromptText("Введите правила грамматики (например, S = aB | b)");
        languageArea.setPromptText("Введите язык в формате L = { a^n1, b^n2 | n1 > 0, n2 = 2 }");

        TextField targetInput = new TextField();
        targetInput.setPromptText("Введите целевую цепочку (например, aab)");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        Button describeLanguageButton = new Button("Описать язык");
        Button translateButton = new Button("Перевести");
        Button outputLeftButton = new Button("Левый вывод");
        Button typeGrammarButton = new Button("Тип грамматики");
        Button deriveButton = new Button("Путь к целевой цепочке");
        Button makeGrammarButton = new Button("Построить грамматику по языку");
        Button compareGrammar = new Button("Сравнение грамматик");
        Button secondGenerator = new Button("Построить КС грамматику");
        Button regularGenerator = new Button("Построить регулярную грамматику");
        CheckBox optimization = new CheckBox("Оптимизировать построение грамматики");
        CheckBox buildTree = new CheckBox("Построить деревья до цепочки");


        // Обработка кнопки "Построить регулярную грамматику"
        regularGenerator.setOnAction(actionEvent -> {
            String input = inputArea.getText();
            FormalLanguage formalLanguage = new FormalLanguage(parseRules(input), "S");
            inputArea.setText(formalLanguage.buildRegular(parseLanguage(formalLanguage.describe())));
        });

        compareGrammar.setOnAction(actionEvent -> {
            TextArea left = new TextArea();
            TextArea right = new TextArea();
            Label answer = new Label("Ответ...");
            Button compare = new Button("Сравнить");
            Button buildRegular = new Button("Построить регулярку для ^");
            Button back = new Button("Назад");
            buildRegular.setOnAction(actionEvent1 -> {
                List<Rule> rulesL = parseRules(left.getText());
                List<Rule> rulesR = parseRules(right.getText());

                FormalLanguage leftL = new FormalLanguage(rulesL, "S");
                FormalLanguage rightL = new FormalLanguage(rulesR, "S");

                List<String> chainsL = leftL.generateChains(100);
                List<String> chainsR = rightL.generateChains(100);

                Set<String> setL = new HashSet<>(chainsL);
                Set<String> setR = new HashSet<>(chainsR);
                Set<String> result = new HashSet<>(setL);
                result.retainAll(setR);
                List<Rule> ruleList = buildGrammarFromChains(result);
                StringBuilder grammar = new StringBuilder();
                for (Rule rule : ruleList) {
                    grammar.append(rule.getKey()).append(" = ").append(rule.getValue()).append("\n");
                }
                left.setText(grammar.toString());
                right.setText("");
//                NFA nfa = new NFA();
//                nfa.buildNFA(ruleList, firstSymbol);
                DFA dfa = buildDFAFromRules(ruleList);
                answer.setText(dfa.toString());
            });


            compare.setOnAction(actionEvent1 -> {

                FormalLanguage grammarL = new FormalLanguage(parseRules(left.getText()), "S");
                FormalLanguage grammarR = new FormalLanguage(parseRules(right.getText()), "S");
                String leftA = grammarL.describe();
                String rightA = grammarR.describe();
                if (leftA.equals(rightA)) {
                    answer.setText("Грамматики эквивалентны");
                } else {
                    answer.setText("Грамматики не эквиваленты");
                }
            });
            HBox high = new HBox(left, right);
            VBox general = new VBox(high, compare, buildRegular, back, answer);

            Scene scene1 = new Scene(general, 500, 400);
            primaryStage.setTitle("Ы");
            primaryStage.setScene(scene1);
            primaryStage.show();
        });

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
            languageArea.setText(result);
        });

        // Генерация КС-языка
        // todo.bug: Отказало
        secondGenerator.setOnAction(actionEvent -> {
            String input = inputArea.getText();
            String language = new FormalLanguage(parseRules(input),firstSymbol).describe();
            System.out.println(language);
            System.out.println(parseLanguage(language));
            inputArea.setText(new Grammar(parseRules(input)).buildGrammar(optimization.isSelected(), parseLanguage(language)));
        });


        // Обработка кнопки "Левый вывод"
        outputLeftButton.setOnAction(event -> {
            String input = inputArea.getText();
            List<Rule> rules = parseRules(input);
            FormalLanguage fl = new FormalLanguage(rules, firstSymbol);
            outputArea.setText(fl.buildLeft(optimization.isSelected()));
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
            if (buildTree.isSelected()) {
                ArrayList<List<String>> sequences = (ArrayList<List<String>>) grammar.makeSequences(target);
                System.out.println(sequences);
                openTreeWindow(sequences, target);
            }
            outputArea.setText(result.toString());
        });

        makeGrammarButton.setOnAction(event -> {
            String input = languageArea.getText();
            Grammar grammar = new Grammar();
            try {
                System.out.println(Objects.requireNonNull(parseLanguage(input)));
            } catch (NullPointerException n) {
                inputArea.setText("Задайте язык");
            }
            inputArea.setText(grammar.buildGrammar(optimization.isSelected(), parseLanguage(input)));
        });

        // Компоновка интерфейса
        FlowPane buttons = new FlowPane(secondGenerator, regularGenerator, compareGrammar, translateButton, outputLeftButton, makeGrammarButton, typeGrammarButton, deriveButton, describeLanguageButton);
        VBox root = new VBox(5, inputArea, languageArea, optimization, buildTree, targetInput, buttons, outputArea);
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Грамматический процессор");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static final double NODE_RADIUS = 20;
    private static final double LEVEL_SPACING = 100;
    private static final double SIBLING_SPACING = 50;

    private void openTreeWindow(List<List<String>> sequences, String target) {
        Pane graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000);

        // Собираем все уникальные состояния и их позиции
        Map<String, NodeInfo> nodes = buildGraphStructure(sequences);

        // Рисуем узлы и связи
        drawGraph(graphPane, nodes, sequences, target);

        // Настраиваем скроллинг
        ScrollPane scrollPane = new ScrollPane(graphPane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        Stage primaryStage = new Stage();

        primaryStage.setTitle("Граф выводов: " + firstSymbol + " → " + target);
        primaryStage.setScene(new Scene(scrollPane, 800, 600));
        primaryStage.show();
    }

    private Map<String, NodeInfo> buildGraphStructure(List<List<String>> sequences) {
        Map<String, NodeInfo> nodes = new LinkedHashMap<>();
        Map<String, Integer> levels = new HashMap<>();

        // Определяем уровни для каждого узла (по максимальной глубине)
        for (List<String> seq : sequences) {
            for (int i = 0; i < seq.size(); i++) {
                String node = seq.get(i);
                levels.merge(node, i, Math::max);
            }
        }

        // Группируем узлы по уровням
        Map<Integer, List<String>> nodesByLevel = new TreeMap<>();
        levels.forEach((node, level) -> {
            nodesByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(node);
        });

        // Рассчитываем позиции для каждого узла
        nodesByLevel.forEach((level, levelNodes) -> {
            double y = 50 + level * LEVEL_SPACING;
            for (int i = 0; i < levelNodes.size(); i++) {
                String node = levelNodes.get(i);
                double x = 100 + i * SIBLING_SPACING;
                nodes.put(node, new NodeInfo(x, y, level));
            }
        });

        return nodes;
    }

    private void drawGraph(Pane pane, Map<String, NodeInfo> nodes, List<List<String>> sequences , String target) {
        // Рисуем связи сначала (чтобы узлы были сверху)
        for (List<String> seq : sequences) {
            for (int i = 0; i < seq.size() - 1; i++) {
                String from = seq.get(i);
                String to = seq.get(i + 1);

                NodeInfo fromInfo = nodes.get(from);
                NodeInfo toInfo = nodes.get(to);

                if (fromInfo != null && toInfo != null) {
                    Line line = new Line(
                            fromInfo.x, fromInfo.y,
                            toInfo.x, toInfo.y
                    );
                    line.getStyleClass().add("graph-edge");
                    pane.getChildren().add(line);
                }
            }
        }

        // Рисуем узлы
        nodes.forEach((node, info) -> {
            Group nodeGroup = new Group();

            // Круг узла
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(
                    info.x, info.y, NODE_RADIUS, Color.CYAN
            );
            circle.getStyleClass().add(node.equals(target) ? "target-node" :
                    node.equals(firstSymbol) ? "start-node" : "normal-node");

            // Текст узла
            Text text = new Text(node);
            text.setX(info.x - text.getLayoutBounds().getWidth()/2);
            text.setY(info.y + text.getLayoutBounds().getHeight()/4);
            text.getStyleClass().add("node-text");

            nodeGroup.getChildren().addAll(circle, text);
            pane.getChildren().add(nodeGroup);
        });
    }

    private static class NodeInfo {
        double x, y;
        int level;

        NodeInfo(double x, double y, int level) {
            this.x = x;
            this.y = y;
            this.level = level;
        }
    }

    public record ParsedLanguage(HashMap<String, String> terminalsAndConstraints,
                                 HashMap<String, String> terminalsGroups,
                                 HashMap<String, String> palindrome,
                                 ArrayList<String> randomTerminals) {}

    // todo: Не переварил L = { a^n0, b^n0 | n0 = 1 }
    public ParsedLanguage parseLanguage(String input) {
        if (isLanguageCorrect(input)) {
            HashMap<String, String> terminalsAndConstraints = getTerminals(input); // терминал - его ограничение a^n | n > 0 -> {a, n>0}
            HashMap<String, String> terminalsGroups = getGroups(input);
            HashMap<String, String> palindrome = getPalindrome(input);
            ArrayList<String> randomTerminals = getRandomTerminals(input); // описаны как <a,b,c,...> E (G)
            return new ParsedLanguage(terminalsAndConstraints, terminalsGroups, palindrome, randomTerminals);
        }
        return null;
    }

    private ArrayList<String> getRandomTerminals(String input) {
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

    private char currentNonTerminal = 'A';

    // todo.tips: А что после Z делать будешь?
    private String getNextNonTerminal() {
        if (currentNonTerminal == 'S') currentNonTerminal++;
        return String.valueOf(currentNonTerminal++);
    }

    public List<Rule> buildGrammarFromChains(Set<String> chains) {
        List<Rule> rules = new ArrayList<>();
        Map<String, String> transitions = new HashMap<>();
        int stateCounter = 0;

        for (String chain : chains) {
            String currentState = "S";
            for (int i = 0; i < chain.length(); i++) {
                String symbol = String.valueOf(chain.charAt(i));
                String key = currentState + "_" + symbol;

                // если уже есть переход — переиспользуем
                String nextState = transitions.getOrDefault(key, null);
                if (nextState == null) {
                    nextState = getNextNonTerminal();
                    transitions.put(key, nextState);
                    rules.add(new Rule(currentState, symbol + nextState));
                }

                currentState = nextState;
            }

            // помечаем конец цепочки ε-переходом (можно заменить на флаг терминального состояния)
            rules.add(new Rule(currentState, "ε"));
        }

        return rules;
    }

    public DFA buildDFAFromRules(List<Rule> rules) {
        DFA dfa = new DFA();

        for (Rule rule : rules) {
            String from = rule.getKey();
            String value = rule.getValue();

            if (value.equals("ε")) {
                dfa.addAcceptingState(from);
            } else {
                char symbol = value.charAt(0);
                String to = value.substring(1);
                dfa.addTransition(from, symbol, to);
            }
        }

        return dfa;
    }

    public static void main(String[] args) {
        launch(args);
    }
}