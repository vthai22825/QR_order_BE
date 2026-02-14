package com.example.qr_order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QrOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrOrderApplication.class, args);
    }

}
