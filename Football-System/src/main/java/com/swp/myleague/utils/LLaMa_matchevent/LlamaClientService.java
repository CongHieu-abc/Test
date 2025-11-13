package com.swp.myleague.utils.LLaMa_matchevent;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LlamaClientService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String generateResponse(String prompt, String modelName) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName); // ví dụ: llama3, mistral, gemma
        body.put("prompt", prompt);
        body.put("stream", false); // false = trả kết quả 1 lần

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL, request, Map.class);
            return (String) response.getBody().get("response");
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi gọi LLaMA: " + e.getMessage();
        }
    }
}
