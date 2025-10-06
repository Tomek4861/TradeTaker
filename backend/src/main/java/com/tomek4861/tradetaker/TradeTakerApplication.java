package com.tomek4861.tradetaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TradeTakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeTakerApplication.class, args);
    }

}
