package com.aris.stock.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    private String symbol;
    private String name;
    private double previousClosingPrice;
    private double currentPrice;
    @JsonIgnore
    private long createdTime;
    private Date createdDate;
    private Double changePercent;


    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }
}