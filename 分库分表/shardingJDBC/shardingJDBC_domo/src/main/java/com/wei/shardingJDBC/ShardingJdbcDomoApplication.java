package com.wei.shardingJDBC;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wei.shardingjdbc_domo.mapper")
public class ShardingJdbcDomoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingJdbcDomoApplication.class, args);
    }

}
