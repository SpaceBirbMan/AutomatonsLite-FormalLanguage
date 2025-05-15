package stud.atmt.atmtlite;

import java.util.HashMap;
import java.util.Map;

public class TuringMachine {
    private String[] states;
    private String[] alphabet;
    private String blankSymbol;       // Пустой символ
    private String[] inputAlphabet;
    private String initialState;
    private String[] finalStates;


    public String getBlankSymbol() {
        return blankSymbol;
    }

    // Лента
    private Map<Integer, String> tape = new HashMap<>();
    private int headPosition = 0;     // Положение головки

    private String currentState;      // Текущее состояние

    // Таблица переходов: Map<текущее состояние, Map<текущий символ, переход>>
    private Map<String, Map<String, Transition>> transitionTable = new HashMap<>();

    // Класс для представления перехода
    public static class Transition {
        public String nextState;
        public String writeSymbol;
        public int moveDirection; // -1 - влево, 0 - остаться, 1 - вправо

        public Transition(String nextState, String writeSymbol, int moveDirection) {
            this.nextState = nextState;
            this.writeSymbol = writeSymbol;
            this.moveDirection = moveDirection;
        }
    }

    public TuringMachine(String[] states, String[] alphabet, String blankSymbol,
                         String[] inputAlphabet, String initialState, String[] finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.blankSymbol = blankSymbol;
        this.inputAlphabet = inputAlphabet;
        this.initialState = initialState;
        this.finalStates = finalStates;
        this.currentState = initialState;
    }

    public void addTransition(String currentState, String currentSymbol,
                              String nextState, String writeSymbol, int moveDirection) {
        if (!transitionTable.containsKey(currentState)) {
            transitionTable.put(currentState, new HashMap<>());
        }
        transitionTable.get(currentState).put(currentSymbol,
                new Transition(nextState, writeSymbol, moveDirection));
    }

    public void initializeTape(String input) {
        tape.clear();
        headPosition = 0;

        for (int i = 0; i < input.length(); i++) {
            String symbol = String.valueOf(input.charAt(i));
            tape.put(i, symbol);
        }
    }

    private String readSymbol() {
        return tape.getOrDefault(headPosition, blankSymbol);
    }

    private void writeSymbol(String symbol) {
        tape.put(headPosition, symbol);
    }

    private void moveHead(int direction) {
        headPosition += direction;
    }

    public boolean step() {
        if (isHalted()) {
            return false;
        }

        String currentSymbol = readSymbol();
        Map<String, Transition> stateTransitions = transitionTable.get(currentState);

        if (stateTransitions == null || !stateTransitions.containsKey(currentSymbol)) {
            return false; // Нет перехода - машина останавливается
        }

        Transition transition = stateTransitions.get(currentSymbol);

        // Выполняем переход
        writeSymbol(transition.writeSymbol);
        moveHead(transition.moveDirection);
        currentState = transition.nextState;

        return true;
    }

    // Проверка, остановилась ли машина
    public boolean isHalted() {
        for (String finalState : finalStates) {
            if (currentState.equals(finalState)) {
                return true;
            }
        }
        return false;
    }

    public String getTapeContents() {
        if (tape.isEmpty()) {
            return "";
        }

        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;

        for (int pos : tape.keySet()) {
            if (pos < minPos) minPos = pos;
            if (pos > maxPos) maxPos = pos;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = minPos; i <= maxPos; i++) {
            sb.append(tape.getOrDefault(i, blankSymbol));
        }

        return sb.toString();
    }

    public String getCurrentState() {
        return currentState;
    }

    public int getHeadPosition() {
        return headPosition;
    }
}