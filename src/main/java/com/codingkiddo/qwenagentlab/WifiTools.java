package com.codingkiddo.qwenagentlab;

public class WifiTools {

    /*
     * --------------------------------------------------
     * TOOL 1: DEVICE INFORMATION
     * --------------------------------------------------
     */
    public static String getDevice(String deviceId) {

        System.out.println();
        System.out.println(
                ">>> JAVA TOOL EXECUTED: getDevice("
                        + deviceId + ")"
        );

        return """
                Device ID: %s
                Device Name: Vinod-iPhone
                Device Type: iPhone 16
                Operating System: iOS
                Connected AP: Bedroom Extender
                Wi-Fi Band: 5 GHz
                """.formatted(deviceId);
    }


    /*
     * --------------------------------------------------
     * TOOL 2: WI-FI TELEMETRY
     * --------------------------------------------------
     */
    public static String getWifiTelemetry(String deviceId) {

        System.out.println();
        System.out.println(
                ">>> JAVA TOOL EXECUTED: getWifiTelemetry("
                        + deviceId + ")"
        );

        return """
                Device ID: %s
                RSSI: -78 dBm
                SNR: 12 dB
                Packet Loss: 8%%
                Retries: 22%%
                PHY Rate: 72 Mbps
                """.formatted(deviceId);
    }


    /*
     * --------------------------------------------------
     * TOOL 3: QoE SCORE
     * --------------------------------------------------
     */
    public static String getQoEScore(String deviceId) {

        System.out.println();
        System.out.println(
                ">>> JAVA TOOL EXECUTED: getQoEScore("
                        + deviceId + ")"
        );

        return """
                Device ID: %s
                QoE Score: 42
                QoE Maximum: 100
                QoE Status: POOR
                """.formatted(deviceId);
    }


    /*
     * --------------------------------------------------
     * TOOL 4: SECURITY RISK
     * --------------------------------------------------
     */
    public static String getSecurityRisk(String deviceId) {

        System.out.println();
        System.out.println(
                ">>> JAVA TOOL EXECUTED: getSecurityRisk("
                        + deviceId + ")"
        );

        return """
                Device ID: %s
                Risk Score: 12
                Risk Maximum: 100
                Risk Status: LOW
                Malware Detected: false
                Suspicious Traffic: false
                """.formatted(deviceId);
    }
}