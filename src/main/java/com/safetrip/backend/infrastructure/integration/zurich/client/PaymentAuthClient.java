package com.safetrip.backend.infrastructure.integration.zurich.client;

import com.safetrip.backend.infrastructure.integration.zurich.dto.request.ZurichAuthRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.ZurichAuthResponse;
import com.safetrip.backend.infrastructure.integration.zurich.service.ZurichConfigService;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.ZurichIntegrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final Long zurichConfigId;

    public PaymentAuthClient(WebClient.Builder webClientBuilder,
                             ZurichConfigService configService,
                             @Value("${zurich.config.id}") Long zurichConfigId) {
        this.webClient = webClientBuilder.build();
        this.configService = configService;
        this.zurichConfigId = zurichConfigId;
    }

    public ZurichAuthResponse authenticate() {
        ZurichIntegrationResponse config = configService.getZurichConfig(zurichConfigId);

        WebClient client = webClient.mutate()
                .baseUrl(config.getBaseUrl())
                .build();

        ZurichAuthRequest request = new ZurichAuthRequest(config.getUsername(), config.getPassword());

        return client.post()
                .uri("/auth/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(request), ZurichAuthRequest.class)
                .retrieve()
                .bodyToMono(ZurichAuthResponse.class)
                .block();
    }
}