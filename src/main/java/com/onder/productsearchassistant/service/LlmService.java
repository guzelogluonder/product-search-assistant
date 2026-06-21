package com.onder.productsearchassistant.service;

import com.onder.productsearchassistant.model.response.SearchResponse;

public interface LlmService {

    SearchResponse parse(String query);

}
