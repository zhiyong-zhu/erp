package com.erp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.erp")
@MapperScan(basePackages = "com.erp.**.mapper")
public class ErpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpAdminApplication.class, args);
    }
}
