package com.jean.fipe_search.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.jean.fipe_search.dto.ConsultaFipeDTO;
import com.jean.fipe_search.service.FipeService;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

    private final RedisConnectionFactory connectionFactory;
    private final String cacheName;
    private final long ttlSeconds;
    private final boolean warmCache;
    private final FipeService fipeService;

    public CacheConfig(
            RedisConnectionFactory connectionFactory,
            @Value("${app.cache.name}") String cacheName,
            @Value("${app.cache.ttl-seconds:6}") long ttlSeconds,
            @Value("${app.cache.warm:false}") boolean warmCache,
            FipeService fipeService) {
        this.connectionFactory = connectionFactory;
        this.cacheName = cacheName;
        this.ttlSeconds = ttlSeconds;
        this.warmCache = warmCache;
        this.fipeService = fipeService;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(warmCache ? 35 : ttlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(ConsultaFipeDTO.class)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .initialCacheNames(Set.of(cacheName))
                .disableCreateOnMissingCache()
                .build();
    }

    @Override
    public CacheResolver cacheResolver() {
        return context -> List.of(
                Objects.requireNonNull(
                        cacheManager().getCache(cacheName),
                        () -> "Cache not found: " + cacheName));
    }

    @Override
    public KeyGenerator keyGenerator() {
        return (_, _, params) -> String.join(":", Arrays.stream(params)
                .map(String::valueOf)
                .toList());
    }

    @EventListener(ApplicationReadyEvent.class)
    protected void warmUpCache() {
        if (!warmCache) {
            LOGGER.info("Cache warm-up desabilitado. Defina app.cache.warm=true para executar.");
            return;
        }

        List<Map<String, Long>> hotKeys = List.of(
                Map.of("modeloId", 1L, "anoModelo", 2023L),
                Map.of("modeloId", 2L, "anoModelo", 2021L),
                Map.of("modeloId", 3L, "anoModelo", 2021L),
                Map.of("modeloId", 4L, "anoModelo", 2021L),
                Map.of("modeloId", 5L, "anoModelo", 2021L));

        LOGGER.info("Iniciando cache warm-up de {} registros", hotKeys.size());

        hotKeys.stream().forEach(hotKey -> {
            Long modeloId = hotKey.get("modeloId");
            Integer anoModelo = hotKey.get("anoModelo").intValue();

            try {
                fipeService.invalidar(modeloId, anoModelo);
                fipeService.consultar(modeloId, anoModelo);
                LOGGER.info("Cache recarregado: modelo={}, ano={}", modeloId, anoModelo);
            } catch (Exception exception) {
                LOGGER.warn(
                        "Falha ao recarregar cache: modelo={}, ano={}",
                        modeloId,
                        anoModelo,
                        exception);
            }
        });

        LOGGER.info("Warmup finalizado. {} itens cacheados.", hotKeys.size());
    }
}