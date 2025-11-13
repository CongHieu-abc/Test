package com.swp.myleague.utils.gemini_matchevent;

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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String API_KEY;

    @Value("${gemini.api.url}")
    private String API_URL;
    
    @Autowired 
    RestTemplate restTemplate;

    public String generate(String prompt) {
        log.info("Gọi Gemini API với prompt length: {} chars", prompt.length());
        log.debug("Prompt content: {}", prompt);
        
        // Tạo request body theo format Gemini API
        Map<String, Object> contentMap = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contentMap, headers);

        try {
            log.debug("Calling Gemini API URL: {}", API_URL);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            
            Map responseBody = response.getBody();
            if (responseBody == null) {
                log.error("Gemini API trả về null response body");
                return "⚠️ Không có phản hồi từ Gemini (null body)";
            }
            
            log.debug("Gemini API response status: {}", response.getStatusCode());
            log.debug("Gemini API response body keys: {}", responseBody.keySet());
            
            // Kiểm tra lỗi từ API
            if (responseBody.containsKey("error")) {
                Map error = (Map) responseBody.get("error");
                String errorMessage = error != null ? error.toString() : "Unknown error";
                log.error("Gemini API error: {}", errorMessage);
                return "❌ Lỗi Gemini API: " + errorMessage;
            }
            
            // Lấy candidates
            Object candidatesObj = responseBody.get("candidates");
            if (candidatesObj == null) {
                log.error("Gemini API response không có 'candidates'. Response: {}", responseBody);
                return "⚠️ Không có phản hồi từ Gemini (no candidates)";
            }
            
            List<Map> candidates = (List<Map>) candidatesObj;
            if (candidates == null || candidates.isEmpty()) {
                log.warn("Gemini API response có candidates nhưng rỗng. Response: {}", responseBody);
                return "⚠️ Không có phản hồi từ Gemini (empty candidates)";
            }
            
            Map first = candidates.get(0);
            if (first == null) {
                log.error("Gemini API candidate đầu tiên là null");
                return "⚠️ Không có phản hồi từ Gemini (null candidate)";
            }
            
            // Kiểm tra finishReason
            Object finishReasonObj = first.get("finishReason");
            if (finishReasonObj != null) {
                String finishReason = finishReasonObj.toString();
                if ("SAFETY".equals(finishReason) || "RECITATION".equals(finishReason) || "OTHER".equals(finishReason)) {
                    log.warn("Gemini API blocked content. Finish reason: {}", finishReason);
                    return "⚠️ Gemini đã chặn nội dung (finishReason: " + finishReason + ")";
                }
                log.debug("Gemini API finishReason: {}", finishReason);
            }
            
            Map content = (Map) first.get("content");
            if (content == null) {
                log.error("Gemini API candidate không có content. Candidate: {}", first);
                return "⚠️ Không có phản hồi từ Gemini (no content)";
            }
            
            List<Map> parts = (List<Map>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                log.error("Gemini API content không có parts. Content: {}", content);
                return "⚠️ Không có phản hồi từ Gemini (no parts)";
            }
            
            String text = (String) parts.get(0).get("text");
            if (text == null || text.trim().isEmpty()) {
                log.error("Gemini API part đầu tiên không có text. Parts: {}", parts);
                return "⚠️ Không có phản hồi từ Gemini (empty text)";
            }
            
            log.info("Gemini API thành công. Response length: {} chars", text.length());
            log.debug("Gemini API response text: {}", text);
            return text;
            
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            return "❌ Lỗi Gemini: " + e.getMessage() + " (Class: " + e.getClass().getSimpleName() + ")";
        }
    }
}
