package stud.atmt.atmtlite;

import java.util.ArrayList;

class AutomataState {
    private final String name;
    private final AutomataStateType stateType;
    private final ArrayList<AutomataTransition> transitions = new ArrayList<>();

    public AutomataState(String name, AutomataStateType type) {
        this.name = name;
        this.stateType = type;
    }

    public String getName() {
        return name;
    }

    public AutomataStateType getStateType() {
        return stateType;
    }

    public ArrayList<AutomataTransition> getTransitions() {
        return transitions;
    }

    public void addTransition(char symbol, AutomataState targetState) {
        transitions.add(new AutomataTransition(symbol, targetState));
    }

    public AutomataState transition(char symbol) {
        for (AutomataTransition transition : transitions) {
            if (transition.symbol() == symbol) {
                return transition.target();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (" + stateType + ")";
    }
}

record AutomataTransition(char symbol, AutomataState target) {
}