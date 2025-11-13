package com.swp.myleague.utils.openai_matchevent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Autowired
    RestTemplate restTemplate;

    public String generateResponse(String prompt) {
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model); // ví dụ: "gpt-3.5-turbo" hoặc "gpt-4o"
        body.put("messages", List.of(message));
        body.put("temperature", 0.7);
        body.put("stream", false); // trả kết quả một lần

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // ⚠️ Thay bằng OpenAI API Key thực tế

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                return (String) messageObj.get("content");
            } else {
                return "❌ Không có phản hồi từ OpenAI.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi gọi OpenAI: " + e.getMessage();
        }
    }
}
