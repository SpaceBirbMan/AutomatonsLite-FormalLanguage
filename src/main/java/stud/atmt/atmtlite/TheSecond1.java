package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TheSecond1 extends Application {

    String firstSymbol = "";
    @Override
    public void start(Stage stage) {
        TextField inputField = new TextField();
        inputField.setPromptText("Введите цепочку, например: 11.010");

        Label resultLabel = new Label();

        TextArea textArea = new TextArea();

        

        Button checkButton = new Button("Проверить");
        checkButton.setOnAction(e -> {
            List<Rule> ruleList = parseRules(textArea.getText());
            NFA nfa = new NFA();
            nfa.buildNFA(ruleList, "S");
            DFA dfa = nfa.toDFA();
            String input = inputField.getText();
            boolean accepted = dfa.accepts(input);
            resultLabel.setText(accepted ? "Подходит " + dfa.toString() : "Не подходит " + dfa.toString());
        });

        VBox layout = new VBox(10, textArea, inputField, checkButton, resultLabel);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Анализ цепочки DFA");
        stage.show();
    }

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
        launch();
    }
}
