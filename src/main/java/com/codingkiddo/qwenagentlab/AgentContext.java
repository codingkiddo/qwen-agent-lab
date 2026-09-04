package com.codingkiddo.qwenagentlab;

import java.util.EnumSet;
import java.util.Set;

public class AgentContext {

    private final String customerQuestion;

    private final StringBuilder history =
            new StringBuilder();

    private final Set<AgentAction> executedActions =
            EnumSet.noneOf(AgentAction.class);

    public AgentContext(String customerQuestion) {
        this.customerQuestion = customerQuestion;
    }

    public String customerQuestion() {
        return customerQuestion;
    }

    /*
     * Record the result of a tool execution.
     */
    public void addToolResult(
            AgentAction action,
            String result) {

        executedActions.add(action);

        history.append("""
                
                TOOL EXECUTED:
                %s
                
                TOOL RESULT:
                %s
                
                """.formatted(
                action,
                result
        ));
    }

    /*
     * Has this tool already been executed?
     */
    public boolean hasExecuted(
            AgentAction action) {

        return executedActions.contains(action);
    }

    /*
     * Context that will be sent back to Qwen.
     */
    public String history() {

        if (history.isEmpty()) {
            return "No tools have been executed yet.";
        }

        return history.toString();
    }

    public Set<AgentAction> executedActions() {
        return Set.copyOf(executedActions);
    }
}