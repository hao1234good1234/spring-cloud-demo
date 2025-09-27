package com.happy;


import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // 开启服务注册
@MapperScan(basePackages = "com.happy.mapper")
@EnableAutoDataSourceProxy  // ✅ user-service 也必须加！否则无法注册分支事务！Seata AT 模式数据源代理，feign调用传递全局事务id
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}