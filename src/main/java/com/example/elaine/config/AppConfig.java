package com.example.elaine.config;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@AllArgsConstructor
public class AppConfig {

    private final DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
    }
}

