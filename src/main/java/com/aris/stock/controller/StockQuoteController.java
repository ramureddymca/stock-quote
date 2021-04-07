package com.aris.stock.controller;

import com.aris.stock.model.ClientHealthState;
import com.aris.stock.model.InputRequest;
import com.aris.stock.model.QuoteRequest;
import com.aris.stock.model.QuoteResponse;
import com.aris.stock.service.StockQuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Controller
public class StockQuoteController {

    @Autowired
    private StockQuoteService stockQuoteService;

  /*  @MessageMapping("greetings")
    Flux<QuoteResponse> greet(
            RSocketRequester clientRSocketConnection,
            @AuthenticationPrincipal Mono<UserDetails> user, InputRequest request) {
        return user.map(UserDetails::getUsername)
                .just(request)
                .flatMapMany(gr -> this.requestResponse(clientRSocketConnection, request));
    }*/


    @MessageMapping("stock-quote")
    public Flux<QuoteResponse> requestResponse(RSocketRequester clientRSocketConnection, InputRequest request) {
        Flux<ClientHealthState> clientHealth = clientRSocketConnection
                .route("health")
                .retrieveFlux(ClientHealthState.class)
                .filter(chs -> !chs.isHealthy())
                .doOnNext(chs -> log.info("not healthy! "));

        log.info("Received notification for my-request-response: " + request.toString());
        List<String> symbols = ofNullable(request.getQuoteRequests()).get().stream().map(QuoteRequest::getSymbol).collect(Collectors.toList());
        return stockQuoteService.getStocksBySymbols(symbols);
    }
    @MessageMapping("request-stream")
    public Flux<QuoteResponse> requestStream(RSocketRequester clientRSocketConnection, InputRequest request) {
        log.debug("Received notification for my-request-response: " + request.toString());
        Flux<ClientHealthState> clientHealth = clientRSocketConnection
                .route("health")
                .retrieveFlux(ClientHealthState.class)
                .filter(chs -> !chs.isHealthy())
                .doOnNext(chs -> log.info("not healthy! "));
        List<String> symbols = ofNullable(request.getQuoteRequests()).get().stream().map(QuoteRequest::getSymbol).collect(Collectors.toList());
        stockQuoteService.getStocksBySymbols(symbols);
        /*return Flux.interval(Duration.ofSeconds(request.getTimeInterval()))
                .just(symbols)
                .flatMap(symbols1 -> stockQuoteService.getStocksBySymbols(symbols));*/
return Flux.just(1).repeat() // infinite Flux with backpressure
        .delayElements(Duration.ofSeconds(1))
        .just(symbols)
        .flatMap(stockQuoteService::getStocksBySymbols)
        .takeUntilOther(clientHealth) // If client is not available stop sending Stock Quote data
        .doOnError(error -> log.debug("Error occurred {} ", error.getMessage()))
        .doOnComplete(()-> log.debug("Get stock quotes completed "));
    }
    @MessageMapping("getStockQuotesWithTimeAndFilters")
    public Flux<QuoteResponse> getStockQuotesWithTimeAndFilters(InputRequest request) {
        log.info("Received notification for my-request-response: " + request.toString());
        /*return Flux.interval(Duration.ofSeconds(request.getTimeInterval()))
                .just(symbols)
                .flatMap(symbols1 -> stockQuoteService.getStocksBySymbols(symbols));*/
        return Flux.just(1).repeat() // infinite Flux with backpressure
                .delayElements(Duration.ofSeconds(1))
                .just(request)
                .flatMap(stockQuoteService::getStockQuotesWithTimeAndFilters)
                .doOnError(error -> log.debug("Error occurred {} ", error.getMessage()))
                .doOnComplete(()-> log.debug("Get stock quotes completed "));
    }
}
