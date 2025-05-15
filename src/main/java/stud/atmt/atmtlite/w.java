package stud.atmt.atmtlite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class State {
    String name;
    boolean isFinal;
    Map<Character, Set<State>> transitions = new HashMap<>();

    @Override
    public String toString() {
        return name + ' ' + transitions.toString() + ' ' + isFinal;
    }
}

class DFAState {
    Set<State> nfaStates;
    boolean isFinal;
    Map<Character, DFAState> transitions = new HashMap<>();
}