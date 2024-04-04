package com.project.chatbackend.configs;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudDiaryConfig {
    private final String CLOUD_NAME = "dbxogj6oe";
    private final String API_KEY = "967651379553858";
    private final String API_SECRET = "SoImI0NrKq5nMWIJ6kaEnL11cgw";
    @Bean
    public Cloudinary cloudinary(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name",CLOUD_NAME);
        config.put("api_key",API_KEY);
        config.put("api_secret",API_SECRET);
        return new Cloudinary(config);
    }
}
