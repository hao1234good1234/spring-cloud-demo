package com.happy;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // 定时扫描本地事务日志，重发未完成消息
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.happy.api.client")// ⭐ 开启 Feign 客户端
@EnableAutoDataSourceProxy  // ✅ 必须加！Seata AT 模式数据源代理，feign调用传递全局事务id
@MapperScan(basePackages = "com.happy.mapper")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}

