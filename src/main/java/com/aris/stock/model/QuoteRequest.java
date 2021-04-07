package com.aris.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {

    private String symbol;
    private String condition;

    public QuoteRequest(String symbol){
        this.symbol = symbol;
     }
}
