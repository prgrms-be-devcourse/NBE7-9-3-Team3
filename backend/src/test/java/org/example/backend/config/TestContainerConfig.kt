package org.example.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import java.util.function.Supplier

class TestContainerConfig {

    /*
    static(companion object) 선언을 통해, 클래스 단위로 컨테이너 생성/종료
    만약 static이 아니라면, 메소드 단위로 컨테이너 생성/종료
     */
    companion object {
        @Container
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0.33").apply { start() }

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
