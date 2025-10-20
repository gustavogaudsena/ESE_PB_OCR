package br.com.ocr.ocr_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OcrApiApplication {

    public static void main(String[] args) {
        System.setProperty("oracle.bmc.sdk.default.httpclient", "jersey");
        SpringApplication.run(OcrApiApplication.class, args);
    }

}
