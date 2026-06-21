package com.onder.productsearchassistant.controller;

import com.onder.productsearchassistant.model.response.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(
                HealthResponse.builder()
                        .status("UP")
                        .timeStamp(Instant.now().toString())
                        .build()
        );
    }

}
