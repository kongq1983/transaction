package com.kq.spring.more.datasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Fox
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MultipleDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleDatasourceApplication.class, args);
    }

}
