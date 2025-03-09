package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GrammarApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Основные элементы интерфейса
        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Введите правила грамматики (например, S = aB | b)");

        TextField targetInput = new TextField();
        targetInput.setPromptText("Введите целевую цепочку (например, aab)");

        Button describeLanguageButton = new Button("Описать язык");
        Button translateButton = new Button("Перевести");
        Button outputLeftButton = new Button("Левый вывод");
        Button typeGrammarButton = new Button("Тип грамматики");
        Button deriveButton = new Button("Путь к целевой цепочке");
        Button makeTreeButton = new Button("Построить дерево вывода");

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
            // todo: Генерим кучу цепочек, ищем закономерности (насколько друг от друга количества терминалов отличаются)
            //  терминалы потырим из цепочек непосредственно
            //  на основании этого сделаем вывод и описание
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

        // Обработка кнопки "Построить дерево вывода"
        makeTreeButton.setOnAction(event -> {

        });

        // Компоновка интерфейса
        VBox root = new VBox(10, inputArea, targetInput, /*translateButton, outputLeftButton, makeTreeButton,*/ typeGrammarButton, deriveButton, describeLanguageButton, outputArea);
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Грамматический процессор");
        primaryStage.setScene(scene);
        primaryStage.show();
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