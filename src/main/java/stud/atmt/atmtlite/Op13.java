package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;


public class Op13 extends Application {

    private static final double STATE_RADIUS = 30;
    private static final double LEVEL_SPACING = 150;
    private static final double STATE_SPACING = 100;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);

        Label label = new Label();

        List<Rule> rules13 = new ArrayList<>();

        rules13.add(new Rule("S", "B0"));
        rules13.add(new Rule("S", "0"));
        rules13.add(new Rule("B", "B0"));
        rules13.add(new Rule("B", "C1"));
        rules13.add(new Rule("B", "0"));
        rules13.add(new Rule("B", "1"));
        rules13.add(new Rule("C", "B0"));

//        rules13.add(new Rule("S", "Sb"));
//        rules13.add(new Rule("S", "Aa"));
//        rules13.add(new Rule("S", "a"));
//        rules13.add(new Rule("S", "b"));
//        rules13.add(new Rule("A", "Aa"));
//        rules13.add(new Rule("A", "Sb"));
//        rules13.add(new Rule("A", "a"));

        FormalLanguage fl = new FormalLanguage(rules13, "S");

        NFA nfa = new NFA();
        nfa.buildNFA(rules13, "S");

        root.getChildren().add(label);

        label.setText(fl.describe());

        drawNFA(root, nfa);

        primaryStage.setTitle("NFA State Diagram");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawNFA(Pane pane, NFA nfa) {
        Set<String> states = nfa.getStates();
        Map<String, Map<Character, Set<String>>> transitions = nfa.getTransitions();
        String startState = nfa.getStartState();
        Set<String> finalStates = nfa.getAcceptingStates();

        // Расположение состояний на сцене
        Map<String, double[]> statePositions = calculateStatePositions(states);

        // Рисуем переходы
        for (String fromState : transitions.keySet()) {
            Map<Character, Set<String>> stateTransitions = transitions.get(fromState);
            for (Character symbol : stateTransitions.keySet()) {
                for (String toState : stateTransitions.get(symbol)) {
                    drawTransition(pane, statePositions, fromState, toState,
                            symbol != null ? symbol.toString() : "ε");
                }
            }
        }

        // Рисуем состояния
        for (String state : states) {
            drawState(pane, statePositions, state,
                    state.equals(startState),
                    finalStates.contains(state));
        }
    }

    private Map<String, double[]> calculateStatePositions(Set<String> states) {
        Map<String, double[]> positions = new HashMap<>();
        int level = 0;
        int countInLevel = 0;

        // Сначала размещаем стартовое состояние
        positions.put("S", new double[]{100, 300});

        // Затем другие состояния
        for (String state : states) {
            if (!state.equals("S") && !state.equals("$")) {
                double x = 300 + level * LEVEL_SPACING;
                double y = 100 + countInLevel * STATE_SPACING;
                positions.put(state, new double[]{x, y});

                countInLevel++;
                if (countInLevel > 3) {
                    level++;
                    countInLevel = 0;
                }
            }
        }

        // Размещаем финальное состояние справа
        if (states.contains("$")) {
            positions.put("$", new double[]{700, 300});
        }

        return positions;
    }

    private void drawState(Pane pane, Map<String, double[]> positions,
                           String state, boolean isStart, boolean isFinal) {
        double[] pos = positions.get(state);
        if (pos == null) return;

        double x = pos[0];
        double y = pos[1];

        Circle circle = new Circle(x, y, STATE_RADIUS);
        circle.setFill(javafx.scene.paint.Color.WHITE);
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeWidth(2);

        if (isFinal) {
            Circle innerCircle = new Circle(x, y, STATE_RADIUS - 5);
            innerCircle.setFill(javafx.scene.paint.Color.WHITE);
            innerCircle.setStroke(javafx.scene.paint.Color.BLACK);
            innerCircle.setStrokeWidth(2);
            pane.getChildren().add(innerCircle);
        }

        Text text = new Text(x - (state.length() * 5), y + 5, state);
        pane.getChildren().addAll(circle, text);

        if (isStart) {
            drawStartArrow(pane, x - STATE_RADIUS - 20, y);
        }
    }

    private void drawStartArrow(Pane pane, double x, double y) {
        Line line = new Line(x, y, x + 15, y);
        Line arrow1 = new Line(x + 15, y, x + 5, y - 5);
        Line arrow2 = new Line(x + 15, y, x + 5, y + 5);

        pane.getChildren().addAll(line, arrow1, arrow2);
    }

    private void drawTransition(Pane pane, Map<String, double[]> positions,
                                String fromState, String toState, String symbol) {
        double[] fromPos = positions.get(fromState);
        double[] toPos = positions.get(toState);
        if (fromPos == null || toPos == null) return;

        double fromX = fromPos[0];
        double fromY = fromPos[1];
        double toX = toPos[0];
        double toY = toPos[1];

        if (fromState.equals(toState)) {
            drawLoopTransition(pane, fromX, fromY, symbol);
        } else {
            drawStraightTransition(pane, fromX, fromY, toX, toY, symbol);
        }
    }

    private void drawStraightTransition(Pane pane, double fromX, double fromY,
                                        double toX, double toY, String symbol) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        double startX = fromX + STATE_RADIUS * Math.cos(angle);
        double startY = fromY + STATE_RADIUS * Math.sin(angle);
        double endX = toX - STATE_RADIUS * Math.cos(angle);
        double endY = toY - STATE_RADIUS * Math.sin(angle);

        Line line = new Line(startX, startY, endX, endY);

        // Стрелка
        double arrowAngle = Math.atan2(endY - startY, endX - startX);
        double arrowLength = 10;
        double arrowX1 = endX - arrowLength * Math.cos(arrowAngle - Math.PI / 6);
        double arrowY1 = endY - arrowLength * Math.sin(arrowAngle - Math.PI / 6);
        double arrowX2 = endX - arrowLength * Math.cos(arrowAngle + Math.PI / 6);
        double arrowY2 = endY - arrowLength * Math.sin(arrowAngle + Math.PI / 6);

        Line arrow1 = new Line(endX, endY, arrowX1, arrowY1);
        Line arrow2 = new Line(endX, endY, arrowX2, arrowY2);

        double textX = (startX + endX) / 2;
        double textY = (startY + endY) / 2;

        double perpendicularAngle = angle + Math.PI/2;
        double offset = 15;
        textX += offset * Math.cos(perpendicularAngle);
        textY += offset * Math.sin(perpendicularAngle);

        Text text = new Text(textX, textY, symbol);
        text.setStyle("-fx-font-size: 14px;");

        pane.getChildren().addAll(line, arrow1, arrow2, text);
    }

    private void drawLoopTransition(Pane pane, double x, double y, String symbol) {
        Circle loop = new Circle(x, y - STATE_RADIUS - 10, 20);
        loop.setFill(javafx.scene.paint.Color.TRANSPARENT);
        loop.setStroke(javafx.scene.paint.Color.BLACK);

        Line arrow = new Line(x + 15, y - STATE_RADIUS - 5, x + 5, y - STATE_RADIUS - 15);

        Text text = new Text(x - 5, y - STATE_RADIUS - 30, symbol);
        text.setStyle("-fx-font-size: 14px;");

        pane.getChildren().addAll(loop, arrow, text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

