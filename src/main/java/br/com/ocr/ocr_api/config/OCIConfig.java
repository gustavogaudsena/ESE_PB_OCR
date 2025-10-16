package br.com.ocr.ocr_api.config;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.aidocument.AIServiceDocumentClient;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OCIConfig {

    @Bean
    public AIServiceDocumentClient aiServiceDocumentClient() throws IOException {
        var configFile = ConfigFileReader.parseDefault();
        var provider = new ConfigFileAuthenticationDetailsProvider(configFile);
        return AIServiceDocumentClient.builder().build(provider);
    }

}