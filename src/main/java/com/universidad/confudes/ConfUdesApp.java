package com.universidad.confudes;

import com.universidad.confudes.externo.qrcheck.QRCheckClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConfUdesApp {
    public static void main(String[] args) {
        SpringApplication.run(ConfUdesApp.class, args);
    }

    @Bean
    public QRCheckClient qrCheckClient() {
        return new QRCheckClient();
    }
}