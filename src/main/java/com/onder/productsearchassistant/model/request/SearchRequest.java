package com.onder.productsearchassistant.model.request;

import com.onder.productsearchassistant.validation.NoOffensiveWords;
import lombok.Data;

@Data
public class SearchRequest {

    @NoOffensiveWords
    private String query;

}
