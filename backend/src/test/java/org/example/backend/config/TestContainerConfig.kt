package org.example.backend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import java.util.function.Supplier

@TestConfiguration
class TestContainerConfig {

    companion object {

        // 컨테이너 선언
        @Container
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0.33")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")

        // 컨테이너 실행
        init {
            mysql.start()
        }

        @DynamicPropertySource
        fun registerMySQLProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", Supplier { mysql.jdbcUrl })
            registry.add("spring.datasource.username", Supplier { mysql.username })
            registry.add("spring.datasource.password", Supplier { mysql.password })
        }
    }

    @Bean
    fun mysqlContainer(): MySQLContainer<*> {
        return mysql
    }
}
