package com.onder.productsearchassistant.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String error;
    private int status;

}
