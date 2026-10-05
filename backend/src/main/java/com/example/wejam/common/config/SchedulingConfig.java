package com.example.wejam.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on {@code @Scheduled} jobs. Off in tests, which call jobs directly so results are deterministic. */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnBooleanProperty(name = "wejam.jobs.enabled", matchIfMissing = true)
class SchedulingConfig {
}
