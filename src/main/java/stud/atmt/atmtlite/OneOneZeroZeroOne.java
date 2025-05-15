package stud.atmt.atmtlite;

public class OneOneZeroZeroOne {
    private DFA dfa;

    public OneOneZeroZeroOne() {
        this.dfa = new DFA();
        buildDFA();
    }

    private void buildDFA() {

        // S - начальное состояние (чётное число единиц, последний символ не 1)
        // Q1 - нечётное число единиц, последний символ 1
        // Q2 - нечётное число единиц, последний символ не 1
        // Q3 - чётное число единиц, последний символ 1

        dfa.getAlphabet().add('0');
        dfa.getAlphabet().add('1');

        dfa.getStates().add("S");
        dfa.getStates().add("Q1");
        dfa.getStates().add("Q2");
        dfa.getStates().add("Q3");

        dfa.addAcceptingState("S"); // Если автомат стопнет на accepting, цепочка принята
        dfa.addAcceptingState("Q3");


        dfa.addTransition("S", '0', "S");  // 0 не меняет чётность единиц
        dfa.addTransition("S", '1', "Q1"); // 1 делает число единиц нечётным

        dfa.addTransition("Q1", '0', "Q2"); // после 1 может идти только 0

        dfa.addTransition("Q2", '0', "Q2"); // 0 не меняет чётность
        dfa.addTransition("Q2", '1', "Q3"); // 1 делает число единиц чётным

        dfa.addTransition("Q3", '0', "S");  // после 1 может идти только 0
    }

    public boolean accepts(String input) {
        return dfa.accepts(input);
    }

    public DFA getDFA() {
        return dfa;
    }

    public static void main(String[] args) {
        OneOneZeroZeroOne automaton = new OneOneZeroZeroOne();

        // Тестирование
        String[] testCases = {
                "",         // true
                "0",        // true
                "1",        // false
                "00",       // true
                "01",       // false
                "10",       // false
                "11",       // false
                "010",      // true
                "0101",     // false
                "0100",     // true
                "01001",    // false
                "010010",   // true
                "0100101"   // false
        };

        for (String test : testCases) {
            System.out.printf("'%s': %b%n", test, automaton.accepts(test));
        }

        System.out.println("\nNFA Structure:");
        System.out.println(automaton.getDFA().toString());
    }
}