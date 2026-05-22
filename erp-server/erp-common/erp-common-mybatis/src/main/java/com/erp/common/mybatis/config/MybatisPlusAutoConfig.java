package com.erp.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.erp.common.mybatis.type.UuidTypeHandler;
import java.util.UUID;
import org.apache.ibatis.type.JdbcType;
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
            configuration.getTypeHandlerRegistry().register(UUID.class, UuidTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(UUID.class, JdbcType.OTHER, UuidTypeHandler.class);
        };
    }
}
