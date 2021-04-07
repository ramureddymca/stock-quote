package com.aris.stock.util;

import com.aris.stock.model.InputRequest;
import com.aris.stock.model.QuoteRequest;
import com.aris.stock.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Dummy Price generator for stocks. We maintain generated price in Map key as symbol value as stock quote object
 */
@Slf4j
@Component
public class PriceGenerator {

    public final static Map<String, List<Stock>> concurrentHashMap = new ConcurrentHashMap<>();
    private static final float MIN_PRICE = 1.0f;
    private static final float MAX_PRICE = 100.0f;
    float previousClosingPrice = 1.0f;

    @Scheduled(fixedRate = 1000)
    public void generatePrice(){

        float finalPreviousClosingPrice = previousClosingPrice;
        float currentPrice = getNextPrice(finalPreviousClosingPrice);
        previousClosingPrice = currentPrice;
        List<Stock> stockSymbols = Arrays.asList(new Stock("GOOG", "Google"),
                new Stock("AMZN", "Amazon"),
                new Stock("AAPL", "Apple"),
                new Stock("MSFT", "Microsoft"),
                new Stock("C", "Citigroup"));

        stockSymbols.forEach(stockWithSymbol -> {
            List<Stock> stocks = concurrentHashMap.get(stockWithSymbol.getSymbol());
            Stock stock = createStock(stockWithSymbol, finalPreviousClosingPrice, currentPrice);
            if(stocks == null){
                List<Stock> newStocks = new ArrayList<>();
                newStocks.add(stock);
                concurrentHashMap.put(stockWithSymbol.getSymbol(), newStocks);
            }else{
                stocks.add(stock);
                concurrentHashMap.put(stockWithSymbol.getSymbol(), stocks);
            }
        });

       // log.debug("Price generated at {}", new Date().toString());
    }

    private Stock createStock(Stock stockWithSymbol, float previousClosingPrice, float currentPrice) {
        Stock stock = new Stock();
        stock.setSymbol(stockWithSymbol.getSymbol());
        stock.setName(stockWithSymbol.getName());
        stock.setPreviousClosingPrice(previousClosingPrice);
        stock.setCurrentPrice(currentPrice);
        stock.setCreatedTime(now().getEpochSecond());
        return  stock;
    }

    private float getNextPrice(float oldPrice)
    {
        float volatility = new Random().nextFloat() * 10 + 2;
        float rnd = new Random().nextFloat();
        float changePercent = 2 * volatility * rnd;

        if (changePercent > volatility) {
            changePercent -= (2 * volatility);
        }
        float changeAmount = oldPrice * changePercent/100;
        float newPrice = oldPrice + changeAmount;

        if (newPrice < MIN_PRICE) {
            newPrice += Math.abs(changeAmount) * 2;
        } else if (newPrice > MAX_PRICE) {
            newPrice -= Math.abs(changeAmount) * 2;
        }

        return newPrice;
    }

    public List<Stock> getStockQuoteBySymbols(List<String> symbols){
        List<Stock> stocks = symbols.stream().map(concurrentHashMap::get)
                .map(stockList -> stockList.get(stockList.size() - 1))
                .collect(toList());
        log.debug("Stock quote price: {}", LocalDateTime.now());
        return stocks;
    }
    public List<Stock> getStockQuotesBySymbols(InputRequest inputRequest){
        List<String> symbols = ofNullable(inputRequest.getQuoteRequests()).get().stream().map(QuoteRequest::getSymbol).collect(Collectors.toList());
        List<Stock>  previousStockPrices = new ArrayList<>();
        symbols.forEach(symbol -> {
            List<Stock> stockList=  concurrentHashMap.get(symbol);
            int index = inputRequest.getTimeInterval() < stockList.size() ? (int) (stockList.size() - inputRequest.getTimeInterval()) : 0;
            previousStockPrices.add(stockList.get(index));
        });
       /* List<Stock> previousStockPrices = symbols.stream().map(concurrentHashMap::get)
                .map(stockList -> stockList.get((int) (stockList.size() - inputRequest.getTimeInterval())))
                .collect(toList());*/
        log.debug("Stock quote price: {}", LocalDateTime.now().getSecond());
        return previousStockPrices;
    }

}
