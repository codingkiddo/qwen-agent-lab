package com.codingkiddo.qwenagentlab;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AgentDecisionTest {

    public static void main(String[] args)
            throws Exception {

        ObjectMapper objectMapper =
                new ObjectMapper();

        String json = """
                {
                  "action": "GET_WIFI_TELEMETRY",
                  "deviceId": "device-123",
                  "answer": null
                }
                """;

        AgentDecision decision =
                objectMapper.readValue(
                        json,
                        AgentDecision.class
                );

        System.out.println(
                "Decision = " + decision
        );

        System.out.println(
                "Action = " + decision.action()
        );

        System.out.println(
                "Device = " + decision.deviceId()
        );

        System.out.println(
                "Answer = " + decision.answer()
        );
    }
}