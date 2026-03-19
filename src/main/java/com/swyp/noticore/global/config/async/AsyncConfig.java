package com.swyp.noticore.global.config.async;

import java.util.concurrent.Executor;
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
     *
     * <p>I/O bound 작업(외부 API 호출)이므로 동시 요청 수 × 채널 수만큼 스레드를 확보한다.
     * 부하테스트 기준 20 VU × 4채널(Email/SMS/OnCall/Slack) = 80개 동시 task 처리를 위해 maxPoolSize=80 설정.
     * queueCapacity=0 으로 설정해 task 제출 즉시 새 스레드를 생성(maxPoolSize까지)한다.
     * maxPoolSize 초과 시 CallerRunsPolicy로 호출 스레드에서 직접 실행하여 task 유실을 방지한다.
     */
    @Bean(name = "channelExecutor")
    public Executor channelExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(80);
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("channel-async-");
        executor.initialize();
        return executor;
    }
}
