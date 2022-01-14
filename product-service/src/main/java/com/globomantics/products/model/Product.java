package com.globomantics.products.model;


import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class Product {

    private Integer id;
    private String name;
    private Integer quantity;
    private Integer version;
}
