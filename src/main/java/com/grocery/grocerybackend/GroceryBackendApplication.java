package com.grocery.grocerybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.grocery.grocerybackend.mapper")
public class GroceryBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroceryBackendApplication.class, args);
    }
}

