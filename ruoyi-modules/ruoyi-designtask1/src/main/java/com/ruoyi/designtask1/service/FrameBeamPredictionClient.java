package com.ruoyi.designtask1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FrameBeamPredictionClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${designtask.frame-beam.surrogate-base-url:http://127.0.0.1:9822}")
    private String frameBeamSurrogateBaseUrl;

    public Map<String, Object> predictGrowth(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
            frameBeamSurrogateBaseUrl + "/api/frame-beam-crack/growth-predict",
            request,
            Map.class
        );
        if (response == null) {
            throw new IllegalStateException("Frame beam crack prediction service returned an empty response.");
        }
        return response;
    }
}
