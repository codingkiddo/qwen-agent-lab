package com.codingkiddo.qwenagentlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class WifiAgent {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL =
            "qwen3:4b";

    /*
     * Protection against an infinite agent loop.
     */
    private static final int MAX_ITERATIONS = 10;

    /*
     * For this learning lab we want the agent
     * to collect all four types of evidence
     * before producing its final diagnosis.
     *
     * Later we will relax this and allow the
     * model/policy layer to decide what is required.
     */
    private static final Set<AgentAction> REQUIRED_TOOLS =
            Set.of(
                    AgentAction.GET_DEVICE,
                    AgentAction.GET_WIFI_TELEMETRY,
                    AgentAction.GET_QOE_SCORE,
                    AgentAction.GET_SECURITY_RISK
            );

    private static final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final HttpClient httpClient =
            HttpClient.newHttpClient();


    public static void main(String[] args)
            throws Exception {

        String customerQuestion =
                "Why is Wi-Fi slow for device-123?";

        runAgent(customerQuestion);
    }


    /*
     * ==================================================
     * AGENT LOOP
     * ==================================================
     */
    static void runAgent(String customerQuestion)
            throws Exception {

        /*
         * This object survives across all iterations.
         *
         * It contains:
         *
         * - original customer question
         * - tool results
         * - tools already executed
         */
        AgentContext context =
                new AgentContext(customerQuestion);


        for (int iteration = 1;
             iteration <= MAX_ITERATIONS;
             iteration++) {

            printSection(
                    "AGENT ITERATION " + iteration
            );


            /*
             * ------------------------------------------
             * STEP 1
             *
             * Build prompt using everything we
             * currently know.
             * ------------------------------------------
             */
            String decisionPrompt =
                    buildDecisionPrompt(context);


            /*
             * ------------------------------------------
             * STEP 2
             *
             * Ask Qwen:
             *
             * "What should I do next?"
             * ------------------------------------------
             */
            String decisionJson =
                    askQwenJson(decisionPrompt);


            System.out.println();
            System.out.println(
                    ">>> AGENT DECISION"
            );

            System.out.println(
                    decisionJson
            );


            /*
             * ------------------------------------------
             * STEP 3
             *
             * Convert Qwen JSON into a Java object.
             * ------------------------------------------
             */
            AgentDecision decision =
                    objectMapper.readValue(
                            decisionJson,
                            AgentDecision.class
                    );


            validateDecision(decision);


            AgentAction action =
                    decision.action();


            /*
             * ------------------------------------------
             * STEP 4
             *
             * Did Qwen decide it has enough data?
             * ------------------------------------------
             */
            if (action
                    == AgentAction.FINAL_ANSWER) {

                /*
                 * Java policy check.
                 *
                 * The LLM cannot finish early in
                 * this particular lab.
                 */
                Set<AgentAction> missingTools =
                        findMissingTools(context);


                if (!missingTools.isEmpty()) {

                    System.out.println();
                    System.out.println(
                            ">>> FINAL ANSWER BLOCKED"
                    );

                    System.out.println(
                            ">>> Missing required tools: "
                                    + missingTools
                    );

                    continue;
                }


                printSection(
                        "FINAL ANSWER"
                );

                System.out.println(
                        decision.answer()
                );

                return;
            }


            /*
             * ------------------------------------------
             * STEP 5
             *
             * Prevent duplicate tool execution.
             * ------------------------------------------
             */
            if (context.hasExecuted(action)) {

                System.out.println();
                System.out.println(
                        ">>> DUPLICATE TOOL REQUEST BLOCKED"
                );

                System.out.println(
                        ">>> Tool already executed: "
                                + action
                );

                continue;
            }


            /*
             * ------------------------------------------
             * STEP 6
             *
             * Java executes the requested tool.
             * ------------------------------------------
             */
            String toolResult =
                    executeTool(decision);


            printSection(
                    "TOOL RESULT: " + action
            );

            System.out.println(
                    toolResult
            );


            /*
             * ------------------------------------------
             * STEP 7
             *
             * Update the agent's context.
             *
             * Next iteration Qwen will receive
             * this result.
             * ------------------------------------------
             */
            context.addToolResult(
                    action,
                    toolResult
            );


            System.out.println();
            System.out.println(
                    ">>> EXECUTED ACTIONS"
            );

            System.out.println(
                    context.executedActions()
            );
        }


        /*
         * If we reach this point the model never
         * reached FINAL_ANSWER.
         */
        throw new IllegalStateException(
                "Agent exceeded maximum iterations: "
                        + MAX_ITERATIONS
        );
    }


    /*
     * ==================================================
     * BUILD AGENT PROMPT
     * ==================================================
     */
    static String buildDecisionPrompt(
            AgentContext context) {

        Set<AgentAction> missingTools =
                findMissingTools(context);


        return """
                You are a Wi-Fi support AI agent.

                Your job is to diagnose the customer's
                Wi-Fi problem using application tools.

                AVAILABLE TOOLS
                ----------------

                GET_DEVICE
                Calls:
                getDevice(deviceId)

                Purpose:
                Returns device information.


                GET_WIFI_TELEMETRY
                Calls:
                getWifiTelemetry(deviceId)

                Purpose:
                Returns current Wi-Fi radio and
                connectivity telemetry.


                GET_QOE_SCORE
                Calls:
                getQoEScore(deviceId)

                Purpose:
                Returns the calculated QoE score.


                GET_SECURITY_RISK
                Calls:
                getSecurityRisk(deviceId)

                Purpose:
                Returns security-risk information.


                ORIGINAL CUSTOMER QUESTION
                --------------------------

                %s


                INFORMATION ALREADY COLLECTED
                -----------------------------

                %s


                ACTIONS ALREADY EXECUTED
                ------------------------

                %s


                REQUIRED TOOLS NOT YET EXECUTED
                -------------------------------

                %s


                RULES
                -----

                1. Choose exactly ONE next action.

                2. Never request a tool that has
                   already been executed.

                3. For this learning lab, all four tools
                   must be executed before FINAL_ANSWER.

                4. Base the final diagnosis only on
                   information contained in tool results.

                5. Do not invent telemetry, thresholds,
                   normal ranges, security findings,
                   measurements, devices or scores.

                6. If required tools remain, choose
                   one of those tools.

                7. Once all required tools have been
                   executed, return FINAL_ANSWER.


                FOR A TOOL CALL RETURN:

                {
                  "action": "GET_WIFI_TELEMETRY",
                  "deviceId": "device-123",
                  "answer": null
                }


                WHEN FINISHED RETURN:

                {
                  "action": "FINAL_ANSWER",
                  "deviceId": "device-123",
                  "answer": "<your grounded diagnosis>"
                }


                Valid actions:

                GET_DEVICE
                GET_WIFI_TELEMETRY
                GET_QOE_SCORE
                GET_SECURITY_RISK
                FINAL_ANSWER


                Return ONLY valid JSON.

                Do not return markdown.

                Do not return explanations outside
                the JSON object.
                """.formatted(
                context.customerQuestion(),
                context.history(),
                context.executedActions(),
                missingTools
        );
    }


    /*
     * ==================================================
     * FIND MISSING REQUIRED TOOLS
     * ==================================================
     */
    static Set<AgentAction> findMissingTools(
            AgentContext context) {

        Set<AgentAction> missing =
                new LinkedHashSet<>(
                        REQUIRED_TOOLS
                );

        missing.removeAll(
                context.executedActions()
        );

        return missing;
    }


    /*
     * ==================================================
     * JAVA TOOL DISPATCHER
     * ==================================================
     *
     * Qwen requests.
     *
     * Java decides what actual code executes.
     */
    static String executeTool(
            AgentDecision decision) {

        String deviceId =
                decision.deviceId();


        return switch (decision.action()) {

            case GET_DEVICE ->
                    WifiTools.getDevice(
                            deviceId
                    );

            case GET_WIFI_TELEMETRY ->
                    WifiTools.getWifiTelemetry(
                            deviceId
                    );

            case GET_QOE_SCORE ->
                    WifiTools.getQoEScore(
                            deviceId
                    );

            case GET_SECURITY_RISK ->
                    WifiTools.getSecurityRisk(
                            deviceId
                    );

            case FINAL_ANSWER ->
                    throw new IllegalStateException(
                            "FINAL_ANSWER is not a Java tool"
                    );
        };
    }


    /*
     * ==================================================
     * DECISION VALIDATION
     * ==================================================
     */
    static void validateDecision(
            AgentDecision decision) {

        if (decision == null) {

            throw new IllegalStateException(
                    "Qwen returned no decision"
            );
        }


        if (decision.action() == null) {

            throw new IllegalStateException(
                    "Qwen did not return an action"
            );
        }


        /*
         * FINAL_ANSWER requires actual answer text.
         */
        if (decision.action()
                == AgentAction.FINAL_ANSWER) {

            if (decision.answer() == null
                    || decision.answer().isBlank()) {

                throw new IllegalStateException(
                        "FINAL_ANSWER requires answer text"
                );
            }

            return;
        }


        /*
         * Tool calls require a device.
         */
        if (decision.deviceId() == null
                || decision.deviceId().isBlank()) {

            throw new IllegalStateException(
                    "Tool execution requires deviceId"
            );
        }
    }


    /*
     * ==================================================
     * QWEN - STRUCTURED JSON MODE
     * ==================================================
     */
    static String askQwenJson(String prompt)
            throws Exception {

        /*
         * Build enum directly from AgentAction.
         *
         * This prevents the Java enum and
         * JSON schema from drifting apart.
         */
        String[] allowedActions =
                Arrays.stream(
                                AgentAction.values()
                        )
                        .map(Enum::name)
                        .toArray(String[]::new);


        Map<String, Object> properties =
                Map.of(

                        "action", Map.of(
                                "type",
                                "string",

                                "enum",
                                allowedActions
                        ),

                        "deviceId", Map.of(
                                "type",
                                "string"
                        ),

                        "answer", Map.of(
                                "type",
                                new String[]{
                                        "string",
                                        "null"
                                }
                        )
                );


        Map<String, Object> jsonSchema =
                Map.of(
                        "type",
                        "object",

                        "properties",
                        properties,

                        "required",
                        new String[]{
                                "action",
                                "deviceId",
                                "answer"
                        },

                        "additionalProperties",
                        false
                );


        Map<String, Object> requestBody =
                Map.of(
                        "model",
                        MODEL,

                        "prompt",
                        prompt,

                        "stream",
                        false,

                        "think",
                        false,

                        "format",
                        jsonSchema
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


        JsonNode ollamaJson =
                objectMapper.readTree(
                        response.body()
                );


        JsonNode modelResponseNode =
                ollamaJson.get(
                        "response"
                );


        if (modelResponseNode == null) {

            throw new IllegalStateException(
                    "Ollama response does not contain 'response'"
            );
        }


        return modelResponseNode.asText();
    }


    /*
     * ==================================================
     * HTTP VALIDATION
     * ==================================================
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


    /*
     * ==================================================
     * CONSOLE OUTPUT
     * ==================================================
     */
    static void printSection(
            String title) {

        System.out.println();
        System.out.println(
                "========================================"
        );

        System.out.println(title);

        System.out.println(
                "========================================"
        );
    }
}