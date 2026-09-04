package com.codingkiddo.qwenagentlab;

public record AgentDecision(

        AgentAction action,

        String deviceId,

        String answer

) {
}