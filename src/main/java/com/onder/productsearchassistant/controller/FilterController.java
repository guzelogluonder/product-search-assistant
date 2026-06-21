package com.onder.productsearchassistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/filters")
public class FilterController {

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> categories(){
        return ResponseEntity.ok(Map.of(
                "categories",List.of("elbise","pantolon","ayakkabı","telefon","laptop")
        ));
    }

    @GetMapping("/colors")
    public ResponseEntity<Map<String, List<String>>> colors(){
        return ResponseEntity.ok(Map.of(
                "colors",List.of("kırmızı","mavi","siyah","beyaz","yeşil")
        ));
    }

    @GetMapping("/brands")
    public ResponseEntity<Map<String, List<String>>> brands(){
        return ResponseEntity.ok(Map.of(
                "brands",List.of("Samsung","Apple","Nike","Adidas","Zara")
        ));
    }
}
