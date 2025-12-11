/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.base.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Redis 缓存配置
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /**
     * 缓存名称常量
     */
    public static final String USER_PERMISSIONS = "user_permissions";
    public static final String USER_MENUS = "user_menus";
    public static final String DICT_TYPE = "dict_type";
    public static final String DICT_DATA = "dict_data";
    public static final String CONFIG = "config";
    public static final String DEPT_TREE = "dept_tree";

    /**
     * 默认缓存过期时间（小时）
     */
    private static final long DEFAULT_TTL_HOURS = 2;

    /**
     * 权限缓存过期时间（小时）
     */
    private static final long PERMISSION_TTL_HOURS = 1;

    /**
     * 字典缓存过期时间（小时）
     */
    private static final long DICT_TTL_HOURS = 24;

    /**
     * 配置缓存过期时间（小时）
     */
    private static final long CONFIG_TTL_HOURS = 24;

    /**
     * RedisTemplate 配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate 配置完成");
        return template;
    }

    /**
     * 缓存管理器配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(DEFAULT_TTL_HOURS))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(objectMapper())))
            .disableCachingNullValues();

        // 不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 权限缓存：1小时过期
        cacheConfigurations.put(USER_PERMISSIONS, defaultConfig.entryTtl(Duration.ofHours(PERMISSION_TTL_HOURS)));
        cacheConfigurations.put(USER_MENUS, defaultConfig.entryTtl(Duration.ofHours(PERMISSION_TTL_HOURS)));

        // 字典缓存：24小时过期
        cacheConfigurations.put(DICT_TYPE, defaultConfig.entryTtl(Duration.ofHours(DICT_TTL_HOURS)));
        cacheConfigurations.put(DICT_DATA, defaultConfig.entryTtl(Duration.ofHours(DICT_TTL_HOURS)));

        // 配置缓存：24小时过期
        cacheConfigurations.put(CONFIG, defaultConfig.entryTtl(Duration.ofHours(CONFIG_TTL_HOURS)));

        // 部门树缓存：2小时过期
        cacheConfigurations.put(DEPT_TREE, defaultConfig.entryTtl(Duration.ofHours(DEFAULT_TTL_HOURS)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();

        log.info("Redis CacheManager 配置完成，已配置缓存: {}", cacheConfigurations.keySet());
        return cacheManager;
    }

    /**
     * 自定义缓存 Key 生成器
     * 格式: 类名:方法名:参数1_参数2_...
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringJoiner joiner = new StringJoiner("_");
            for (Object param : params) {
                if (param != null) {
                    joiner.add(param.toString());
                }
            }
            return target.getClass().getSimpleName() + ":" + method.getName() + ":" + joiner;
        };
    }

    /**
     * Jackson ObjectMapper 配置
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        // 支持 Java 8 日期时间
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
