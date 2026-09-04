package com.codingkiddo.qwenagentlab;

public class AgentContextTest {

    public static void main(String[] args) {

        String deviceId =
                "device-123";

        AgentContext context =
                new AgentContext(
                        "Why is Wi-Fi slow for device-123?"
                );

        System.out.println(
                "===== INITIAL CONTEXT ====="
        );

        System.out.println(
                context.history()
        );

        /*
         * Call first tool
         */
        String device =
                WifiTools.getDevice(deviceId);

        context.addToolResult(
                AgentAction.GET_DEVICE,
                device
        );

        System.out.println();
        System.out.println(
                "===== AFTER GET_DEVICE ====="
        );

        System.out.println(
                context.history()
        );

        /*
         * Call second tool
         */
        String telemetry =
                WifiTools.getWifiTelemetry(deviceId);

        context.addToolResult(
                AgentAction.GET_WIFI_TELEMETRY,
                telemetry
        );

        System.out.println();
        System.out.println(
                "===== AFTER GET_WIFI_TELEMETRY ====="
        );

        System.out.println(
                context.history()
        );

        System.out.println();
        System.out.println(
                "Executed actions:"
        );

        System.out.println(
                context.executedActions()
        );
    }
}