package com.aris.stock.service;

import com.aris.stock.controller.StockQuoteController;
import com.aris.stock.model.InputRequest;
import com.aris.stock.model.QuoteRequest;
import com.aris.stock.model.QuoteResponse;
import com.aris.stock.util.PriceGenerator;
import com.aris.stock.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class StockQuoteServiceImpl implements StockQuoteService{

    @Autowired
    private PriceGenerator priceGenerator;

    @Override
    public Flux<QuoteResponse> getStocksBySymbols(List<String> symbols) {
        QuoteResponse quoteResponse = new QuoteResponse();
        quoteResponse.setStocks(priceGenerator.getStockQuoteBySymbols(symbols));
        quoteResponse.setStatus("Ok");
        return Flux.just(quoteResponse);

    }
    @Override
    public Flux<QuoteResponse> getStockQuotesWithTimeAndFilters(InputRequest inputRequest) {
        QuoteResponse quoteResponse = new QuoteResponse();
        try {
            List<String> symbols = ofNullable(inputRequest.getQuoteRequests()).get()
                    .stream().map(QuoteRequest::getSymbol)
                    .collect(Collectors.toList());
            Map<String, QuoteRequest> quoteRequestMap = ofNullable(inputRequest.getQuoteRequests()).get()
                    .stream().collect(Collectors.toMap(QuoteRequest::getSymbol, quoteRequest -> quoteRequest));

            List<Stock> currentStocks = priceGenerator.getStockQuoteBySymbols(symbols);
            Map<String, Double> previousPrices = priceGenerator.getStockQuotesBySymbols(inputRequest)
                    .stream().collect(Collectors.toMap(Stock::getSymbol, Stock::getCurrentPrice));
            List<Stock> finalStocks = new ArrayList<>();
            getPriceValueInPercent(quoteRequestMap, currentStocks, previousPrices, finalStocks);
            if (finalStocks != null && finalStocks.size() > 0) {
                quoteResponse.setStocks(finalStocks);
                quoteResponse.setStatus("Ok");
            } else {
                quoteResponse.setStatusMessage("Don't have any stock higher/lower than given condition");
            }
        }catch (Exception e){
            log.error("Error {}",e.getMessage());
        }finally {
            quoteResponse.setStatusMessage("Error occurred, while getting the stock with given conditions");
        }
    return Flux.just(quoteResponse);
    }

    private void getPriceValueInPercent(Map<String, QuoteRequest> quoteRequestMap, List<Stock> currentStocks, Map<String, Double> previousPrices, List<Stock> finalStocks) {
        currentStocks.forEach(stock -> {
           Double previousPrice =  previousPrices.get(stock.getSymbol());
           Double currentPrice = stock.getCurrentPrice();
           Double actualChangePercent = getChangePercent(currentPrice, previousPrice);
           Double changePercent = Double.parseDouble(quoteRequestMap.get(stock.getSymbol()).getCondition());
           stock.setCreatedDate(new Date(stock.getCreatedTime()));
           stock.setChangePercent(actualChangePercent);
            if(actualChangePercent >= changePercent || actualChangePercent <= -changePercent ){
                finalStocks.add(stock);
            }
        });
    }

    private Double getChangePercent(Double currentPrice, Double previousPrice) {
        return (currentPrice - previousPrice) * 100 / previousPrice;
    }
}
