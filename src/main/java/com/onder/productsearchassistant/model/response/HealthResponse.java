package com.onder.productsearchassistant.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthResponse {

    private String status;
    private String timeStamp;

}
