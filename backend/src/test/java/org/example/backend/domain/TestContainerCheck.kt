package org.example.backend.domain

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class TestContainerCheck {

    @Autowired
    private lateinit var dataSource: DataSource

    /**
     * == 테스트코드가 testcontainers를 사용하고 있는 지 검증하는 메소드 ==
     *
     * 테스트코드가 사용하고 있는 DB가 무엇인지,
     * DB와 잘 연결되어 동작하고 있는지(테이블 확인을 통해) 검증
     */
    @Test
    fun `check tables in container`() {
        println("=======================")

        dataSource.connection.use { conn ->
            // 사용하고 있는 JDBC URL 확인
            println("JDBC URL: ${conn.metaData.url}")

            // 테이블 확인
            val rs = conn.createStatement().executeQuery("SHOW TABLES;")
            while (rs.next()) {
                println("Table: ${rs.getString(1)}")
            }
        }
        println("=======================")
    }
}