import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class QwenClient {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final HttpClient httpClient =
            HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {

        String answer = askQwen(
                "Explain Java virtual threads in two sentences."
        );

        System.out.println("Qwen:");
        System.out.println(answer);
    }

    static String askQwen(String prompt) throws Exception {

        Map<String, Object> requestBody = Map.of(
                "model", "qwen3:4b",
                "prompt", prompt,
                "stream", false
        );

        String json =
                objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> httpResponse =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        JsonNode responseJson =
                objectMapper.readTree(httpResponse.body());

        return responseJson
                .get("response")
                .asText();
    }
}