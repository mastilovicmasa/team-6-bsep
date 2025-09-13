package com.team6.bsep.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {
    @Value("${google.recaptcha.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verifyCaptcha(String token) {
        String url = "https://www.google.com/recaptcha/api/siteverify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secret);
        body.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(url, request, Map.class);

        System.out.println("CAPTCHA response: " + resp.getBody());

        return resp.getBody() != null && Boolean.TRUE.equals(resp.getBody().get("success"));
    }
}

