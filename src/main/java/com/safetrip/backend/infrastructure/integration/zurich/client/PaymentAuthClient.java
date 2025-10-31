package com.safetrip.backend.infrastructure.integration.zurich;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentAuthClient {

    private final WebClient webClient;
    private final ZurichConfigService configService;

    public PaymentAuthClient(WebClient.Builder webClientBuilder,
                             ZurichConfigService configService) {
        this.webClient = webClientBuilder.build();
        this.configService = configService;
    }

    public ZurichAuthResponse authenticate(Long zurichParameterId) {
        ZurichIntegrationConfig config = configService.getZurichConfig(zurichParameterId);

        WebClient client = webClient.mutate()
                .baseUrl(config.getBaseUrl())
                .build();

        ZurichAuthRequest request = new ZurichAuthRequest(config.getUsername(), config.getPassword());

        return client.post()
                .uri("/auth/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("X-CSRFToken", config.getCsrfToken())
                .body(Mono.just(request), ZurichAuthRequest.class)
                .retrieve()
                .bodyToMono(ZurichAuthResponse.class)
                .block();
    }
}