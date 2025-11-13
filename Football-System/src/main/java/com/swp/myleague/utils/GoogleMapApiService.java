package com.swp.myleague.utils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Service
public class GoogleMapApiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "AIzaSyBamXOpGnhqjB8a-ZlMuF47JSGdMnhb4OY";

    public Map<String, Object> getLatLngFromAddress(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                UriUtils.encode(address, StandardCharsets.UTF_8) +
                "&key=" + apiKey;
        System.out.println(url);
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody(); // bạn có thể map lại thành DTO nếu muốn
    }
}
