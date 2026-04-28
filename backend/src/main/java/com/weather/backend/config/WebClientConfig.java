package com.weather.backend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(WeatherProperties weatherProperties) {
        long timeoutMillis = weatherProperties.timeout().toMillis();

        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeoutMillis))
            .responseTimeout(weatherProperties.timeout())
            .doOnConnected(connection -> connection
                .addHandlerLast(new ReadTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
            .baseUrl(weatherProperties.baseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
