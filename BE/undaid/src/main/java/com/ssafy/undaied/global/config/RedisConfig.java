package com.ssafy.undaied.global.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.LocalDateTime;

@EnableRedisRepositories
@Configuration
public class RedisConfig {

    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    // ✅ 문자열 저장 전용 (기존 redisTemplate)
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    // ✅ JSON 저장 전용 (복잡한 redis 구조 사용하거나 LocalDateTime 직렬화 할 때 사용)
    @Bean
    public RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configOverride(LocalDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss"));

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}


// package com.ssafy.undaied.global.config;

// import com.fasterxml.jackson.annotation.JsonFormat;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import jakarta.annotation.PostConstruct;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
// import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.StringRedisSerializer;
// import java.time.LocalDateTime;

// @Slf4j
// @EnableRedisRepositories
// @Configuration
// public class RedisConfig {

//     @Value("${spring.redis.host:localhost}")
//     private String redisHost;
    
//     @Value("${spring.redis.port:6379}")
//     private int redisPort;

//     @Bean
//     public RedisConnectionFactory redisConnectionFactory() {
//         LettuceConnectionFactory lcf = new LettuceConnectionFactory(redisHost, redisPort);
//         lcf.setDatabase(0);
//         lcf.afterPropertiesSet();
//         return lcf;
//     }

//     // ✅ 문자열 저장 전용 (기존 redisTemplate)
//     @Bean
//     public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
//         RedisTemplate<String, String> template = new RedisTemplate<>();
//         template.setConnectionFactory(connectionFactory);

//         template.setKeySerializer(new StringRedisSerializer());
//         template.setHashKeySerializer(new StringRedisSerializer());
//         template.setValueSerializer(new StringRedisSerializer());
//         template.setHashValueSerializer(new StringRedisSerializer());

//         template.afterPropertiesSet();
//         return template;
//     }

//     // ✅ JSON 저장 전용 (복잡한 redis 구조 사용하거나 LocalDateTime 직렬화 할 때 사용)
//     @Bean
//     public RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory connectionFactory) {
//         RedisTemplate<String, Object> template = new RedisTemplate<>();
//         template.setConnectionFactory(connectionFactory);

//         ObjectMapper objectMapper = new ObjectMapper();
//         objectMapper.registerModule(new JavaTimeModule());
//         objectMapper.configOverride(LocalDateTime.class)
//                 .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss"));

//         GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

//         template.setKeySerializer(new StringRedisSerializer());
//         template.setHashKeySerializer(new StringRedisSerializer());
//         template.setValueSerializer(serializer);
//         template.setHashValueSerializer(serializer);

//         template.afterPropertiesSet();
//         return template;
//     }

//     @PostConstruct
//     public void testRedisConnection() {
//         try {
//             RedisConnectionFactory factory = redisConnectionFactory();
//             factory.getConnection().ping();
//             log.info("Successfully connected to Redis at {}:{}", redisHost, redisPort);
//         } catch (Exception e) {
//             log.error("Failed to connect to Redis: ", e);
//         }
//     }
// }