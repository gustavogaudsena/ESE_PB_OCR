package br.com.ocr.ocr_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OcrApiApplication {

    public static void main(String[] args) {
        System.setProperty("oracle.bmc.sdk.default.httpclient", "jersey");
        SpringApplication.run(OcrApiApplication.class, args);
    }

}
