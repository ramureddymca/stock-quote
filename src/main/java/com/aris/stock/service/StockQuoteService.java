package com.aris.stock.service;

import com.aris.stock.model.InputRequest;
import com.aris.stock.model.QuoteResponse;
import com.aris.stock.model.Stock;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StockQuoteService {

    public Flux<QuoteResponse> getStocksBySymbols(List<String> symbols);

    public Flux<QuoteResponse> getStockQuotesWithTimeAndFilters(InputRequest inputRequest);
}
