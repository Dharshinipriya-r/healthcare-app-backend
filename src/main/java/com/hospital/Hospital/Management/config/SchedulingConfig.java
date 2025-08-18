package com.hospital.Hospital.Management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
@EnableScheduling
public class SchedulingConfig {

    
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Core configuration
        scheduler.setPoolSize(3); 
        scheduler.setThreadNamePrefix("hospital-scheduler-"); 
        scheduler.setDaemon(false);
        
        
        scheduler.setWaitForTasksToCompleteOnShutdown(true); 
        scheduler.setAwaitTerminationSeconds(30); 
        
       
        scheduler.setRejectedExecutionHandler(
            (runnable, executor) -> {
                System.err.println("Hospital Scheduler: Task rejected - " + runnable.toString());
              
            }
        );
        
       
        scheduler.initialize();
        
        return scheduler;
    }

}
