package com.onder.productsearchassistant.service;

import com.onder.productsearchassistant.model.request.SearchRequest;
import com.onder.productsearchassistant.model.response.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final LlmService llmService;

    public SearchResponse search(SearchRequest request){
        return llmService.parse(request.getQuery());
    }

}
