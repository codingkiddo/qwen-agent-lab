import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class WifiAgent {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL =
            "qwen3:4b";

    private static final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final HttpClient httpClient =
            HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {

        String customerQuestion =
                "Why is Wi-Fi slow for device-123?";

        runAgent(customerQuestion);
    }

    static void runAgent(String customerQuestion)
            throws Exception {

        /*
         * STEP 1
         *
         * Ask Qwen3 what action it wants to perform.
         *
         * Important:
         * Qwen3 does NOT execute Java methods.
         *
         * It only returns a structured request.
         */
        String decisionPrompt = """
                You are a Wi-Fi support AI agent.

                You have access to this tool:

                getWifiTelemetry(deviceId)

                Tool description:
                Returns current Wi-Fi radio and connectivity
                telemetry for the specified device.

                Customer question:

                %s

                Decide what information is required.

                If Wi-Fi telemetry is required,
                return ONLY valid JSON in this format:

                {
                  "action": "GET_WIFI_TELEMETRY",
                  "deviceId": "<device id>"
                }

                Do not answer the customer's question yet.

                Do not add markdown.
                Do not add explanation.
                Return only JSON.
                """.formatted(customerQuestion);

        System.out.println(
                "========================================"
        );
        System.out.println(
                "STEP 1 - CUSTOMER QUESTION"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(customerQuestion);

        /*
         * Ask Qwen3 for a structured decision.
         */
        String decision =
                askQwenJson(decisionPrompt);

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "STEP 2 - QWEN TOOL DECISION"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(decision);

        /*
         * STEP 2
         *
         * Java parses Qwen's decision.
         */
        JsonNode decisionJson =
                objectMapper.readTree(decision);

        JsonNode actionNode =
                decisionJson.get("action");

        if (actionNode == null) {
            throw new IllegalStateException(
                    "Qwen did not return an action"
            );
        }

        String action =
                actionNode.asText();

        /*
         * STEP 3
         *
         * The Java application decides whether
         * this action is allowed.
         *
         * This is very important.
         *
         * LLM REQUESTS the action.
         * Java CONTROLS execution.
         */
        if ("GET_WIFI_TELEMETRY".equals(action)) {

            JsonNode deviceIdNode =
                    decisionJson.get("deviceId");

            if (deviceIdNode == null) {
                throw new IllegalStateException(
                        "Qwen did not return deviceId"
                );
            }

            String deviceId =
                    deviceIdNode.asText();

            /*
             * STEP 4
             *
             * Execute the actual Java tool.
             */
            String telemetry =
                    getWifiTelemetry(deviceId);

            System.out.println();
            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "STEP 3 - TOOL RESULT"
            );
            System.out.println(
                    "========================================"
            );

            System.out.println(telemetry);

            /*
             * STEP 5
             *
             * Give the actual tool result
             * back to Qwen3.
             */
            String finalPrompt = """
                    You are a Wi-Fi troubleshooting assistant.

                    Original customer question:

                    %s

                    The getWifiTelemetry tool returned
                    the following actual data:

                    %s

                    Use ONLY the telemetry above.

                    Do not invent additional measurements.

                    Explain:

                    1. Why the Wi-Fi is likely slow.
                    2. Which metrics support the diagnosis.
                    3. What the most likely root cause is.
                    4. Suggest one or two reasonable next steps.

                    Keep the explanation concise and
                    understandable for a support engineer.
                    """.formatted(
                    customerQuestion,
                    telemetry
            );

            /*
             * STEP 6
             *
             * Qwen3 now reasons using actual telemetry.
             */
            String finalAnswer =
                    askQwenText(finalPrompt);

            System.out.println();
            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "STEP 4 - FINAL QWEN ANSWER"
            );
            System.out.println(
                    "========================================"
            );

            System.out.println(finalAnswer);

        } else {

            /*
             * Fail closed.
             *
             * We do not execute random actions suggested
             * by the LLM.
             */
            throw new IllegalStateException(
                    "Unsupported tool action: " + action
            );
        }
    }

    /*
     * --------------------------------------------------
     * TOOL
     * --------------------------------------------------
     *
     * This is an ordinary Java method.
     *
     * There is nothing AI-specific here.
     *
     * Later this could call:
     *
     * Airties API
     * TimescaleDB
     * Redis
     * PostgreSQL
     * Device Intelligence service
     * Aprecomm
     *
     * For now we return hard-coded lab data.
     */
    static String getWifiTelemetry(String deviceId) {

        System.out.println();
        System.out.println(
                ">>> JAVA TOOL EXECUTED"
        );

        System.out.println(
                ">>> getWifiTelemetry("
                        + deviceId
                        + ")"
        );

        return """
                Device ID: %s
                Connected AP: Bedroom Extender
                RSSI: -78 dBm
                SNR: 12 dB
                Packet Loss: 8%%
                Retries: 22%%
                PHY Rate: 72 Mbps
                """.formatted(deviceId);
    }

    /*
     * --------------------------------------------------
     * CALL QWEN - JSON MODE
     * --------------------------------------------------
     *
     * Used when we want Qwen to return
     * structured JSON.
     */
//    static String askQwenJson(String prompt)
//            throws Exception {
//
//        Map<String, Object> requestBody =
//                Map.of(
//                        "model", MODEL,
//                        "prompt", prompt,
//                        "stream", false,
//                        "format", "json"
//                );
//
//        String requestJson =
//                objectMapper.writeValueAsString(
//                        requestBody
//                );
//
//        HttpRequest request =
//                HttpRequest.newBuilder()
//                        .uri(
//                                URI.create(
//                                        OLLAMA_URL
//                                )
//                        )
//                        .header(
//                                "Content-Type",
//                                "application/json"
//                        )
//                        .POST(
//                                HttpRequest
//                                        .BodyPublishers
//                                        .ofString(
//                                                requestJson
//                                        )
//                        )
//                        .build();
//
//        HttpResponse<String> response =
//                httpClient.send(
//                        request,
//                        HttpResponse
//                                .BodyHandlers
//                                .ofString()
//                );
//
//        validateHttpResponse(response);
//
//        JsonNode responseJson =
//                objectMapper.readTree(
//                        response.body()
//                );
//
//        JsonNode modelResponseNode =
//                responseJson.get("response");
//
//        if (modelResponseNode == null) {
//            throw new IllegalStateException(
//                    "Ollama response does not contain 'response'"
//            );
//        }
//
//        return modelResponseNode.asText();
//    }

    static String askQwenJson(String prompt)
            throws Exception {

        Map<String, Object> properties = Map.of(
                "action", Map.of(
                        "type", "string",
                        "enum", new String[]{
                                "GET_WIFI_TELEMETRY"
                        }
                ),
                "deviceId", Map.of(
                        "type", "string"
                )
        );

        Map<String, Object> jsonSchema = Map.of(
                "type", "object",
                "properties", properties,
                "required", new String[]{
                        "action",
                        "deviceId"
                },
                "additionalProperties", false
        );

        Map<String, Object> requestBody =
                Map.of(
                        "model", MODEL,
                        "prompt", prompt,
                        "stream", false,

                        // Important for Qwen3 in this lab
                        "think", false,

                        // Force the expected structure
                        "format", jsonSchema
                );

        String requestJson =
                objectMapper.writeValueAsString(
                        requestBody
                );

        System.out.println();
        System.out.println(
                ">>> REQUEST SENT TO OLLAMA"
        );

        System.out.println(requestJson);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        OLLAMA_URL
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(
                                                requestJson
                                        )
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        validateHttpResponse(response);

        /*
         * VERY IMPORTANT WHILE LEARNING:
         *
         * Show the complete HTTP response from Ollama.
         */
        System.out.println();
        System.out.println(
                ">>> RAW OLLAMA RESPONSE"
        );

        System.out.println(response.body());

        JsonNode responseJson =
                objectMapper.readTree(
                        response.body()
                );

        JsonNode modelResponseNode =
                responseJson.get("response");

        if (modelResponseNode == null) {
            throw new IllegalStateException(
                    "Ollama response does not contain 'response'"
            );
        }

        String modelResponse =
                modelResponseNode.asText();

        System.out.println();
        System.out.println(
                ">>> MODEL RESPONSE FIELD"
        );

        System.out.println(modelResponse);

        return modelResponse;
    }

    /*
     * --------------------------------------------------
     * CALL QWEN - TEXT MODE
     * --------------------------------------------------
     *
     * Used when we want a normal
     * human-readable answer.
     */
    static String askQwenText(String prompt)
            throws Exception {

        Map<String, Object> requestBody =
                Map.of(
                        "model", MODEL,
                        "prompt", prompt,
                        "stream", false
                );

        String requestJson =
                objectMapper.writeValueAsString(
                        requestBody
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        OLLAMA_URL
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(
                                                requestJson
                                        )
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        validateHttpResponse(response);

        JsonNode responseJson =
                objectMapper.readTree(
                        response.body()
                );

        JsonNode modelResponseNode =
                responseJson.get("response");

        if (modelResponseNode == null) {
            throw new IllegalStateException(
                    "Ollama response does not contain 'response'"
            );
        }

        return modelResponseNode.asText();
    }

    /*
     * --------------------------------------------------
     * SIMPLE HTTP VALIDATION
     * --------------------------------------------------
     */
    static void validateHttpResponse(
            HttpResponse<String> response) {

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "Ollama HTTP call failed. "
                            + "Status="
                            + response.statusCode()
                            + ", body="
                            + response.body()
            );
        }
    }
}