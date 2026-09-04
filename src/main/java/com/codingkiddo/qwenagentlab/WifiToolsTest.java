package com.codingkiddo.qwenagentlab;

public class WifiToolsTest {

    public static void main(String[] args) {

        String deviceId = "device-123";

        System.out.println(
                "================================"
        );
        System.out.println("DEVICE");
        System.out.println(
                "================================"
        );

        System.out.println(
                WifiTools.getDevice(deviceId)
        );


        System.out.println(
                "================================"
        );
        System.out.println("WI-FI TELEMETRY");
        System.out.println(
                "================================"
        );

        System.out.println(
                WifiTools.getWifiTelemetry(deviceId)
        );


        System.out.println(
                "================================"
        );
        System.out.println("QoE");
        System.out.println(
                "================================"
        );

        System.out.println(
                WifiTools.getQoEScore(deviceId)
        );


        System.out.println(
                "================================"
        );
        System.out.println("SECURITY");
        System.out.println(
                "================================"
        );

        System.out.println(
                WifiTools.getSecurityRisk(deviceId)
        );
    }
}