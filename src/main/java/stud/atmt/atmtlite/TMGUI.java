package stud.atmt.atmtlite;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TMGUI extends Application {

    // 10111011+11 в бинарной

    // Дана строка из букв a и b. Разработать МТ, которая переместит все буквы a в
    //левую, а буквы b – в правую часть строки. В состоянии q1 обозревается крайний левый
    //символ строки


    private TuringMachine tm;
    private TextArea tapeDisplay;
    private TextArea stateDisplay;
    private TextArea transitionDisplay;
    private Button stepButton;
    private Button resetButton;
    private ComboBox<String> taskSelector;
    private TextField inputField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Визуализатор Машины Тьюринга");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        HBox taskSelectionBox = new HBox(10);
        taskSelectionBox.setAlignment(Pos.CENTER_LEFT);
        Label taskLabel = new Label("Выберите задание:");
        taskSelector = new ComboBox<>();
        taskSelector.getItems().addAll(
                "1. Бинарное сложение",
                "2. Сортировка"
        );
        taskSelector.getSelectionModel().select(0);
        taskSelectionBox.getChildren().addAll(taskLabel, taskSelector);

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        Label inputLabel = new Label("Входные данные:");
        inputField = new TextField();
        inputField.setPrefWidth(200);
        inputBox.getChildren().addAll(inputLabel, inputField);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        stepButton = new Button("Шаг");
        resetButton = new Button("Сброс");
        buttonBox.getChildren().addAll(stepButton, resetButton);

        Label tapeLabel = new Label("Лента:");
        tapeDisplay = new TextArea();
        tapeDisplay.setEditable(false);
        tapeDisplay.setFont(Font.font("Monospaced", 14));
        tapeDisplay.setStyle("-fx-text-fill: blue;");

        HBox infoBox = new HBox(20);
        VBox stateBox = new VBox(5);
        Label stateLabel = new Label("Состояние:");
        stateDisplay = new TextArea();
        stateDisplay.setEditable(false);
        stateDisplay.setPrefSize(150, 60);
        stateBox.getChildren().addAll(stateLabel, stateDisplay);

        VBox transitionBox = new VBox(5);
        Label transitionLabel = new Label("Последний переход:");
        transitionDisplay = new TextArea();
        transitionDisplay.setEditable(false);
        transitionDisplay.setPrefSize(300, 60);
        transitionBox.getChildren().addAll(transitionLabel, transitionDisplay);

        infoBox.getChildren().addAll(stateBox, transitionBox);

        root.getChildren().addAll(
                taskSelectionBox,
                inputBox,
                buttonBox,
                tapeLabel,
                tapeDisplay,
                infoBox
        );

        taskSelector.setOnAction(e -> resetMachine());
        stepButton.setOnAction(e -> stepMachine());
        resetButton.setOnAction(e -> resetMachine());
        inputField.setOnAction(e -> resetMachine());

        resetMachine();

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void resetMachine() {
        String selectedTask = taskSelector.getSelectionModel().getSelectedItem();
        String input = inputField.getText().trim();

        if (selectedTask.startsWith("1")) {
            if (input.isEmpty()) input = "!10111011+11";
            tm = createBinaryAdditionMachine(input);
        } else {
            if (input.isEmpty()) input = "aabbbaba";
            tm = createSortingMachine(input);
        }

        updateDisplay();
    }

    private void stepMachine() {
        if (tm != null && !tm.isHalted()) {
            tm.step();
            updateDisplay();
        }
    }

    private void updateDisplay() {
        if (tm != null) {
            // Отображаем ленту с выделением текущей позиции
            String tapeStr = tm.getTapeContents();
            int headPos = tm.getHeadPosition();
            int minPos = 0; // Для отображения пустых символов по краям
            int maxPos = tapeStr.length() + 10;

            StringBuilder tapeBuilder = new StringBuilder();
            for (int i = minPos; i <= maxPos; i++) {
                String symbol = i >= 0 && i < tapeStr.length() ?
                        String.valueOf(tapeStr.charAt(i)) : tm.getBlankSymbol();

                if (i == headPos) {
                    tapeBuilder.append("[").append(symbol).append("]");
                } else {
                    tapeBuilder.append(" ").append(symbol).append(" ");
                }
            }
            tapeDisplay.setText(tapeBuilder.toString());
            stateDisplay.setText(tm.getCurrentState() +
                    (tm.isHalted() ? " (остановлена)" : ""));
            transitionDisplay.setText("Последний переход выполнен");
        }
    }

    //todo: Начало ленты - восклицательный знак
    private TuringMachine createBinaryAdditionMachine(String input) {
        String[] states = {
                "Start", "goToRight", "PlaceEquals", "GoToBLast", "RememberAndEraseB",
                "GoToALastWith1", "GoToALastWith0", "RememberAndEraseA1", "RememberAndEraseA0",
                "GoToAnswer1", "GoToAnswer0", "GoToAnswer10", "Step0", "Step1", "Step10",
                "Place0", "Place1", "Place10", "Place10S", "Place10Carry", "GoToEndOfAns", "Pick0", "Pick1",
                "AIsOver", "BIsOver", "halt", "GoToEqualsFromAnswer", "GetALast", "Pick"
        };

        String[] alphabet = {"0", "1", "+", "_", "=", "!"};
        String blankSymbol = "_";
        String[] inputAlphabet = {"0", "1", "+", "!"};
        String initialState = "Start";
        String[] finalStates = {"AIsOver","halt"};

        TuringMachine tm = new TuringMachine(states, alphabet, blankSymbol, inputAlphabet, initialState, finalStates);

        tm.addTransition("Start", "0", "goToRight", "0", 1);
        tm.addTransition("Start", "!", "Start", "!", 1);
        tm.addTransition("Start", "1", "goToRight", "1", 1);
        tm.addTransition("Start", "+", "goToRight", "+", 1);
        tm.addTransition("Start", "_", "PlaceEquals", "_", -1);

        tm.addTransition("goToRight", "0", "goToRight", "0", 1);
        tm.addTransition("goToRight", "1", "goToRight", "1", 1);
        tm.addTransition("goToRight", "+", "goToRight", "+", 1);
        tm.addTransition("goToRight", "_", "PlaceEquals", "_", 0);

        tm.addTransition("PlaceEquals", "_", "GoToBLast", "=", -1);

        tm.addTransition("GoToBLast", "_", "GoToBLast", "_", -1);
        tm.addTransition("GoToBLast", "0", "RememberAndEraseB", "0", 0);
        tm.addTransition("GoToBLast", "1", "RememberAndEraseB", "1", 0);
        tm.addTransition("GoToBLast", "+", "BIsOver", "+", 0);

        tm.addTransition("RememberAndEraseB", "0", "GoToALastWith0", "_", -1);
        tm.addTransition("RememberAndEraseB", "1", "GoToALastWith1", "_", -1);
        tm.addTransition("RememberAndEraseB", "_", "RememberAndEraseB", "_", -1);
        tm.addTransition("RememberAndEraseB", "+", "BIsOver", "_", 0);

        tm.addTransition("GoToALastWith0", "0", "GoToALastWith0", "0", -1);
        tm.addTransition("GoToALastWith0", "1", "GoToALastWith0", "1", -1);
        tm.addTransition("GoToALastWith0", "_", "GoToALastWith0", "1", -1);
        tm.addTransition("GoToALastWith0", "+", "RememberAndEraseA0", "+", -1);
        tm.addTransition("GoToALastWith0", "E", "AIsOver", "_", 1);

        tm.addTransition("GoToALastWith1", "0", "GoToALastWith1", "0", -1);
        tm.addTransition("GoToALastWith1", "1", "GoToALastWith1", "1", -1);
        tm.addTransition("GoToALastWith1", "+", "RememberAndEraseA1", "+", -1);
        tm.addTransition("GoToALastWith1", "!", "AIsOver", "_", 1);

        tm.addTransition("RememberAndEraseA0", "0", "GoToAnswer0", "_", 1);
        tm.addTransition("RememberAndEraseA0", "1", "GoToAnswer1", "_", 1);
        tm.addTransition("RememberAndEraseA0", "_", "RememberAndEraseA0", "_", -1);

        tm.addTransition("RememberAndEraseA1", "0", "GoToAnswer1", "_", 1);
        tm.addTransition("RememberAndEraseA1", "1", "GoToAnswer10", "_", 1);
        tm.addTransition("RememberAndEraseA1", "_", "RememberAndEraseA1", "_", -1);
        // если [a] < [b] то не сработает, нужна реакция на ! и состояние GetBLast

        tm.addTransition("GoToAnswer0", "0", "GoToAnswer0", "0", 1);
        tm.addTransition("GoToAnswer0", "1", "GoToAnswer0", "1", 1);
        tm.addTransition("GoToAnswer0", "+", "GoToAnswer0", "+", 1);
        tm.addTransition("GoToAnswer0", "_", "GoToAnswer0", "_", 1);

        tm.addTransition("GoToAnswer1", "0", "GoToAnswer1", "0", 1);
        tm.addTransition("GoToAnswer1", "1", "GoToAnswer1", "1", 1);
        tm.addTransition("GoToAnswer1", "+", "GoToAnswer1", "+", 1);
        tm.addTransition("GoToAnswer1", "_", "GoToAnswer1", "_", 1);

        tm.addTransition("GoToAnswer10", "0", "GoToAnswer10", "0", 1);
        tm.addTransition("GoToAnswer10", "1", "GoToAnswer10", "1", 1);
        tm.addTransition("GoToAnswer10", "+", "GoToAnswer10", "+", 1);
        tm.addTransition("GoToAnswer10", "_", "GoToAnswer10", "_", 1);

        tm.addTransition("GoToAnswer0", "=", "Step0", "=", 1);
        tm.addTransition("GoToAnswer1", "=", "Step1", "=", 1);
        tm.addTransition("GoToAnswer10", "=", "Place10", "=", 1);

        tm.addTransition("Step0", "_", "Place0", "_", 1);
        tm.addTransition("Step1", "_", "Place1", "_", 1);;

        tm.addTransition("Place1", "_", "GoToEndOfAns", "1", 1);
        tm.addTransition("Place1", "1", "Place1", "0", -1);

        tm.addTransition("Place0", "_", "GoToEndOfAns", "0", 1);
        tm.addTransition("Place0", "1", "GoToEndOfAns", "1", 1);

        tm.addTransition("Place10", "_", "Place10S", "1", 1);
        tm.addTransition("Place10S", "_", "GoToEndOfAns", "0", 1);
        tm.addTransition("Place10S", "1", "GoToEndOfAns", "1", 1);

        tm.addTransition("GoToEndOfAns", "_", "Pick", "_", -1);
        tm.addTransition("GoToEndOfAns", "1", "GoToEndOfAns", "1", 1);
        tm.addTransition("GoToEndOfAns", "0", "GoToEndOfAns", "0", 1);

        tm.addTransition("Pick", "0", "Pick0", "_", 1);
        tm.addTransition("Pick", "1", "Pick1", "_", 1);
        tm.addTransition("Pick", "_", "Pick", "_", -1);

        tm.addTransition("Pick1", "_", "Pick", "1", -1);
        tm.addTransition("Pick1", "1", "Pick1", "1", 1);
        tm.addTransition("Pick1", "0", "Pick1", "0", 1);

        tm.addTransition("Pick0", "_", "Pick", "0", -1);
        tm.addTransition("Pick0", "1", "Pick0", "1", 1);
        tm.addTransition("Pick0", "0", "Pick0", "0", 1);

        tm.addTransition("Pick", "=", "GoToBLast", "=", -1);

        tm.addTransition("BIsOver", "+", "GetALast", "+", -1);
        tm.addTransition("GetALast", "!", "AIsOver", "!", 0);

        tm.addTransition("GetALast", "_", "GetALast", "_", -1);
        tm.addTransition("GetALast", "+", "GetALast", "+", -1);
        tm.addTransition("GetALast", "=", "GetALast", "=", -1);
        tm.addTransition("GetALast", "1", "GoToAnswer1", "_", 1);
        tm.addTransition("GetALast", "0", "GoToAnswer0", "_", 1);

        // Дальше размещение цифры, смещение всего числа
        // идём максимально вправо (GoToEndOfAns) -> шаг влево -> берём число(PickX)(если _ -> влево, если "=" -> начинаем поиск крайнего бита B) -> шаг вправо -> ставим() -> повторять до "равно"

        tm.initializeTape(input);
        return tm;
    }


    private TuringMachine createSortingMachine(String input) {
        // Добавляем новое состояние для перемещения в начало очистки и сам проход преобразования
        String[] states = {"scan", "seekBlank", "rewind", "rewindCleanup", "convert", "halt"};
        String[] alphabet = {"a", "b", "B", "_"};
        String blankSymbol = "_";
        String[] inputAlphabet = {"a", "b"};
        String initialState = "scan";
        String[] finalStates = {"halt"};

        TuringMachine tm = new TuringMachine(
                states, alphabet, blankSymbol, inputAlphabet, initialState, finalStates);

        // scan: ищем первую 'b'
        tm.addTransition("scan", "a", "scan", "a", 1);
        tm.addTransition("scan", "B", "scan", "B", 1);
        tm.addTransition("scan", "b", "seekBlank", "_", 1); // нашли b, стерли её и переходим к поиску места для B
        tm.addTransition("scan", "_", "rewindCleanup", "_", -1); // если достигли конца — начинаем очистку

        // seekBlank: ищем первую пустую клетку для вставки 'B'
        tm.addTransition("seekBlank", "a", "seekBlank", "a", 1);
        tm.addTransition("seekBlank", "b", "seekBlank", "b", 1);
        tm.addTransition("seekBlank", "B", "seekBlank", "B", 1);
        tm.addTransition("seekBlank", "_", "rewind", "B", -1);

        // rewind: возвращаемся к началу исходной части для нового прохода
        tm.addTransition("rewind", "a", "rewind", "a", -1);
        tm.addTransition("rewind", "b", "rewind", "b", -1);
        tm.addTransition("rewind", "B", "rewind", "B", -1);
        tm.addTransition("rewind", "_", "scan", "_", 1);

        // rewindCleanup: возвращаемся в начало ленты для этапа преобразования
        tm.addTransition("rewindCleanup", "a", "rewindCleanup", "a", -1);
        tm.addTransition("rewindCleanup", "b", "rewindCleanup", "b", -1);
        tm.addTransition("rewindCleanup", "B", "rewindCleanup", "B", -1);
        tm.addTransition("rewindCleanup", "_", "convert", "_", 1);

        // convert: заменяем все B -> b до конца ленты и останавливаемся
        tm.addTransition("convert", "a", "convert", "a", 1);
        tm.addTransition("convert", "b", "convert", "b", 1);
        tm.addTransition("convert", "B", "convert", "b", 1);
        tm.addTransition("convert", "_", "halt", "_", 0);

        tm.initializeTape(input);
        return tm;
    }

}
