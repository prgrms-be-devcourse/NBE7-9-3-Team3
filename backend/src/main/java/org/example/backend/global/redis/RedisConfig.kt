package org.example.backend.global.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host:localhost}")
    private val host: String = "localhost"

    @Value("\${spring.data.redis.port:6379}")
    private val port: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private val password: String? = null

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration()
        redisConfig.hostName = host
        redisConfig.port = port
        password?.takeIf { it.isNotEmpty() }?.let { redisConfig.setPassword(it) }

        return LettuceConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        template.setEnableTransactionSupport(false)
        template.afterPropertiesSet()
        return template
    }
}

