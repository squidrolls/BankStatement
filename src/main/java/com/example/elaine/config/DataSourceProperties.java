package com.example.elaine.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="spring.datasource")
@Getter
@Setter
public class DataSourceProperties {
    private String url;
    @NotNull
    private String username;
    @NotNull
    private String password;
}

