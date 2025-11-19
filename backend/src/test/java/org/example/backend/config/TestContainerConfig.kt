package org.example.backend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class TestContainerConfig {

    /*
    static(companion object) 선언을 통해, 클래스 단위로 컨테이너 생성/종료
    만약 static이 아니라면, 메소드 단위로 컨테이너 생성/종료
     */
    companion object {

        // 컨테이너 선언
        @Container
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0.33")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")

        @Container
        val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)

        // 컨테이너 실행
        init {
            mysql.start()
            redis.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerMySQLProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379).toString() }
        }
    }

    @Bean
    fun mysqlContainer(): MySQLContainer<*> {
        return mysql
    }

    @Bean
    fun redisContainer(): GenericContainer<*> {
        return redis
    }

    @Bean
    @Primary
    fun testRedisConnectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration()
        redisConfig.hostName = redis.host
        redisConfig.port = redis.getMappedPort(6379)
        return LettuceConnectionFactory(redisConfig)
    }
}
