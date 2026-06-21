package com.onder.productsearchassistant.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Neden @JsonInclude(NON_NULL)? Kullanıcı sadece "kırmızı elbise" derse brand,
                                            // size gibi alanlar null olur. Bunları JSON'a dahil etmeyiz — temiz response döner.
public class SearchResponse {
    private String category;
    private String color;
    private String brand;
    private String model;
    private Integer priceFrom;
    private Integer priceTo;
    private String currency;
    private String size;
    private Boolean freeShipping;
}
