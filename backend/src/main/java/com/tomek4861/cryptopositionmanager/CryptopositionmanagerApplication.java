package com.tomek4861.cryptopositionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptopositionmanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptopositionmanagerApplication.class, args);
    }

}
