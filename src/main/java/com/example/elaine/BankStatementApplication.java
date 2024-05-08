package com.example.elaine;

import com.example.elaine.config.DataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
public class BankStatementApplication {
	public static void main(String[] args) {
		SpringApplication.run(BankStatementApplication.class, args);
	}

}
