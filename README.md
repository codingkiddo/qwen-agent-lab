# Qwen AI Agent Lab — Plain Java + Ollendorfer/Ollama

This project is a hands-on learning lab for understanding how an AI agent works under the hood before introducing frameworks such as Spring AI.

The application uses:

- Java 21
- Ollama
- Qwen3:4B
- Java HttpClient
- Jackson
- JSON Schema structured output

The goal is to understand the complete interaction between a Java application, a locally hosted LLM, and application tools.

## Current Flow

A customer asks:

"Why is Wi-Fi slow for device-123?"

The application follows this flow:

Customer Question
        ↓
Java Application
        ↓
Qwen3:4B via Ollama
        ↓
LLM decides that Wi-Fi telemetry is required
        ↓
Structured JSON tool request

{
  "action": "GET_WIFI_TELEMETRY",
  "deviceId": "device-123"
}

        ↓
Java parses and validates the request
        ↓
Java executes

getWifiTelemetry("device-123")

        ↓
Telemetry is returned

RSSI: -78 dB
SNR: 12 dB
Packet Loss: 8%
Retries: 22%
PHY Rate: 72 Mbps

        ↓
Telemetry is sent back to Qwen3
        ↓
Qwen3 reasons using the retrieved data
        ↓
Final Wi-Fi diagnosis

## What This Lab Demonstrates

### 1. Java → Local LLM

The Java application communicates directly with Ollama over HTTP:

Java
  ↓ HTTP/JSON
Ollama
  ↓
Qwen3:4B

No Spring AI or other AI framework is used yet.

### 2. Structured LLM Output

Instead of relying on free-form text, the application asks Qwen3 to produce structured JSON.

Example:

{
  "action": "GET_WIFI_TELEMETRY",
  "deviceId": "device-123"
}

A JSON Schema is provided to constrain the expected response.

### 3. LLM Tool Selection

Qwen3 determines that additional information is required and requests the available tool:

getWifiTelemetry(deviceId)

The LLM does NOT execute the Java method directly.

### 4. Java-Controlled Tool Execution

The Java application:

1. Parses the LLM response
2. Validates the requested action
3. Checks whether the action is supported
4. Executes the Java method
5. Returns the result to the LLM

Important principle:

LLM requests an action.
Java controls and executes the action.

### 5. Grounded AI Response

Instead of asking Qwen3 to guess why Wi-Fi is slow, real application data is supplied as context.

This demonstrates the difference between:

LLM knowledge

and

LLM + application data + tools

## Current Tool

getWifiTelemetry(deviceId)

For now, this tool returns simulated Wi-Fi telemetry.

Later it can be connected to real systems such as:

- Airties Wi-Fi telemetry
- TimescaleDB
- PostgreSQL
- Redis
- Kafka-derived device state
- Device Intelligence services
- QoE platforms

## Key Learning

An AI Agent is not simply an LLM.

A simplified agent architecture is:

LLM
 +
Tools
 +
Tool selection
 +
Application orchestration
 +
Validation
 +
Guardrails
 +
State/context

The model decides what information it needs, while the application remains responsible for authorization and execution.

## Next Step

Extend the application into a multi-tool agent with:

- getDevice()
- getWifiTelemetry()
- getQoEScore()
- getSecurityRisk()

Qwen3 will then be allowed to repeatedly select tools until it has enough information to produce a final answer.

After understanding the manual agent loop, the same architecture will be rebuilt using Spring AI and @Tool.