package br.com.ocr.ocr_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
//        return new LettuceConnectionFactory("redis.stockflow.svc.cluster.local", 6379);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory f) {
        return new StringRedisTemplate(f);
    }
}
