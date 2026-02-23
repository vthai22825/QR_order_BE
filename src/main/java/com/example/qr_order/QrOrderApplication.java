package com.example.qr_order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@SpringBootApplication
@EnableJpaAuditing
public class QrOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrOrderApplication.class, args);
    }

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    // Thêm hàm này vào để hoàn tất Bước 2
    @Bean
    public PayOS payOS() {
        ClientOptions options = ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
                .build();
        return new PayOS(options);
    }

}
