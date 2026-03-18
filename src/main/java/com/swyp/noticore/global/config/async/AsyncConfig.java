package com.swyp.noticore.global.config.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 알림 이벤트 리스너 전용 스레드풀.
     * AFTER_COMMIT 이벤트를 비동기로 처리한다.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notification-async-");
        executor.initialize();
        return executor;
    }

    /**
     * 알림 채널(Email/SMS/OnCall/Slack) 병렬 실행 전용 스레드풀.
     * 4개 채널이 동시에 실행되므로 고정 사이즈 4로 설정한다.
     */
    @Bean(name = "channelExecutor")
    public Executor channelExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
