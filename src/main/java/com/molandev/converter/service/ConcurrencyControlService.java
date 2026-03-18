package com.molandev.converter.service;

import com.molandev.converter.config.ConverterConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Concurrency control service
 * Uses semaphore to limit the number of concurrent document conversions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrencyControlService {
    
    private final ConverterConfig converterConfig;
    private Semaphore semaphore;
    
    @PostConstruct
    public void init() {
        int maxConcurrent = converterConfig.getConcurrency().getMaxConcurrent();
        semaphore = new Semaphore(maxConcurrent);
        log.info("Initialized concurrency control: max concurrent = {}, acquire timeout = {} seconds", 
                maxConcurrent, 
                converterConfig.getConcurrency().getAcquireTimeout());
    }
    
    /**
     * Acquire execution permit
     * 
     * @return true if permit acquired successfully, false if timeout
     * @throws InterruptedException if waiting is interrupted
     */
    public boolean acquire() throws InterruptedException {
        int timeout = converterConfig.getConcurrency().getAcquireTimeout();
        boolean acquired = semaphore.tryAcquire(timeout, TimeUnit.SECONDS);
        
        if (acquired) {
            log.debug("Acquired concurrency permit, available permits: {}", semaphore.availablePermits());
        } else {
            log.warn("Concurrency permit acquisition timeout, system is busy");
        }
        
        return acquired;
    }
    
    /**
     * Release execution permit
     */
    public void release() {
        semaphore.release();
        log.debug("Released concurrency permit, available permits: {}", semaphore.availablePermits());
    }
    
    /**
     * Get current available permits
     */
    public int availablePermits() {
        return semaphore.availablePermits();
    }
    
    /**
     * Get max concurrent count
     */
    public int getMaxConcurrent() {
        return converterConfig.getConcurrency().getMaxConcurrent();
    }
}
