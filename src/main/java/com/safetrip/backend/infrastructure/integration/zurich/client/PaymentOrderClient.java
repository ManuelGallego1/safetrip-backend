package com.safetrip.backend.infrastructure.integration.zurich.client;

import com.safetrip.backend.infrastructure.integration.zurich.dto.request.AddOrderPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.LinkCobroRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.request.StatusPaymentRequest;
import com.safetrip.backend.infrastructure.integration.zurich.dto.response.*;
import com.safetrip.backend.infrastructure.integration.zurich.service.ZurichConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentOrderClient {

    private final WebClient webClient;
    private final ZurichConfigService configService;
    private final PaymentAuthClient paymentAuthClient;

    @Value("${zurich.config.id}")
    private Long zurichConfigId;

    @Value("${zurich.confirmation-url}")
    private String confirmationUrl;

    @Value("${zurich.response-url}")
    private String responseUrl;

    public PaymentOrderClient(WebClient.Builder webClientBuilder,
                              ZurichConfigService configService,
                              PaymentAuthClient paymentAuthClient) {
        this.webClient = webClientBuilder.build();
        this.configService = configService;
        this.paymentAuthClient = paymentAuthClient;
    }

    public AddOrderPaymentResponse sendOrder(AddOrderPaymentRequest addOrderPaymentRequest) {
        ZurichAuthResponse authResponse = paymentAuthClient.authenticate();
        String token = authResponse.getToken();
        ZurichIntegrationResponse config = configService.getZurichConfig(zurichConfigId);

        WebClient client = webClient.mutate()
                .baseUrl(config.getBaseUrl())
                .build();

        log.info("🚀 Enviando AddOrderPayment a Zurich: {}", addOrderPaymentRequest);

        return client.post()
                .uri("/api/addOrderPayment")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Mono.just(addOrderPaymentRequest), AddOrderPaymentRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        log.info("✅ Zurich respondió 200 OK en addOrderPayment");
                        return response.bodyToMono(AddOrderPaymentResponse.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("❌ Zurich addOrderPayment devolvió {} con body:\n{}", response.statusCode(), body);
                                    return Mono.error(new RuntimeException("Zurich addOrderPayment error: " + response.statusCode() + " - " + body));
                                });
                    }
                })
                .block();
    }

    public LinkCobroResponse getLinkCobroVoucher(String voucher) {
        ZurichAuthResponse authResponse = paymentAuthClient.authenticate();
        String token = authResponse.getToken();
        ZurichIntegrationResponse config = configService.getZurichConfig(zurichConfigId);

        WebClient client = webClient.mutate()
                .baseUrl(config.getBaseUrl())
                .build();

        LinkCobroRequest request = new LinkCobroRequest(voucher, confirmationUrl, responseUrl);

        log.info("🔗 Solicitando LinkCobroVoucher con payload: {}", request);

        return client.post()
                .uri("/api/getLinkCobroVoucher")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Mono.just(request), LinkCobroRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        log.info("✅ Zurich respondió 200 OK en getLinkCobroVoucher");
                        return response.bodyToMono(LinkCobroResponse.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("❌ Zurich getLinkCobroVoucher devolvió {} con body:\n{}", response.statusCode(), body);
                                    return Mono.error(new RuntimeException("Zurich getLinkCobroVoucher error: " + response.statusCode() + " - " + body));
                                });
                    }
                })
                .block();
    }

    public StatusPaymentResponse getStatusPayment(String voucher) {
        ZurichAuthResponse authResponse = paymentAuthClient.authenticate();
        String token = authResponse.getToken();
        ZurichIntegrationResponse config = configService.getZurichConfig(zurichConfigId);

        WebClient client = webClient.mutate()
                .baseUrl(config.getBaseUrl())
                .build();

        StatusPaymentRequest request = new StatusPaymentRequest(voucher);

        log.info("🔗 Solicitando info pago: {}", request);

        return client.post()
                .uri("/api/getVoucher")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(Mono.just(request), StatusPaymentRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        log.info("✅ Zurich respondió 200 OK en getStatusPayment");
                        return response.bodyToMono(StatusPaymentResponse.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("❌ Zurich getStatusPayment devolvió {} con body:\n{}", response.statusCode(), body);
                                    return Mono.error(new RuntimeException("Zurich getStatusPayment error: " + response.statusCode() + " - " + body));
                                });
                    }
                })
                .block();
    }


}