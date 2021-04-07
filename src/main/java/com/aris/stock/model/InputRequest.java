package com.aris.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@Setter
@Getter
public class InputRequest {

    private long  timeInterval;
    private List<QuoteRequest> quoteRequests;

    public InputRequest(List<QuoteRequest> quoteRequests){
        this.quoteRequests = quoteRequests;
    }


}
