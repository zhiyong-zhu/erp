package com.erp.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.erp.common.mybatis.type.StringArrayTypeHandler;
import com.erp.common.mybatis.type.UuidTypeHandler;
import java.util.UUID;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusAutoConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer uuidTypeHandlerCustomizer() {
        return configuration -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UUID.class, UuidTypeHandler.class);
            registry.register(UUID.class, JdbcType.OTHER, UuidTypeHandler.class);
            registry.register(String[].class, StringArrayTypeHandler.class);
        };
    }
}
