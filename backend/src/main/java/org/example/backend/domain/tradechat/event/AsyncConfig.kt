package org.example.backend.domain.tradechat.event

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean(name = ["taskExecutor"])
    fun threadPoolTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5    // 기본 스레드 수
        executor.maxPoolSize = 10    // 최대 스레드 수
        executor.queueCapacity = 50  // 대기 큐 용량
        executor.setThreadNamePrefix("ChatAsync-")
        executor.initialize()
        return executor
    }
}

