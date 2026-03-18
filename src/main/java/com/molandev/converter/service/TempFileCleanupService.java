package com.molandev.converter.service;

import com.molandev.converter.config.ConverterConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

/**
 * Temporary file cleanup service
 * Periodically cleans up temporary files older than retention time
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TempFileCleanupService {
    
    private final ConverterConfig converterConfig;
    /**
     * -- GETTER --
     *  Get temporary file base directory
     */
    @Getter
    private Path tempBaseDir;
    
    @PostConstruct
    public void init() {
        try {
            // Use configured temporary directory
            String tempDirPath = converterConfig.getCleanup().getTempDir();
            tempBaseDir = Paths.get(tempDirPath);
            
            if (!Files.exists(tempBaseDir)) {
                Files.createDirectories(tempBaseDir);
                log.info("Created temporary file base directory: {}", tempBaseDir);
            }
            
            log.info("Temporary file base directory: {}", tempBaseDir);
            log.info("File retention time: {} minutes", converterConfig.getCleanup().getRetentionMinutes());
            log.info("Cleanup task interval: {} minutes", converterConfig.getCleanup().getScheduleMinutes());
        } catch (IOException e) {
            log.error("Failed to initialize temporary file directory", e);
            throw new RuntimeException("Failed to initialize temporary file directory", e);
        }
    }

    /**
     * Periodically clean up expired temporary files
     * Uses configured interval, default every 5 minutes
     */
    @Scheduled(fixedDelayString = "#{${converter.cleanup.schedule-minutes:5} * 60 * 1000}", 
               initialDelayString = "#{${converter.cleanup.schedule-minutes:5} * 60 * 1000}")
    public void cleanupExpiredFiles() {
        log.info("Starting to cleanup expired temporary files...");
        
        if (!Files.exists(tempBaseDir)) {
            log.warn("Temporary file directory does not exist: {}", tempBaseDir);
            return;
        }
        
        int retentionMinutes = converterConfig.getCleanup().getRetentionMinutes();
        long expirationTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(retentionMinutes);
        
        try {
            int[] counts = {0, 0}; // [删除的目录数, 删除的文件数]
            
            Files.walkFileTree(tempBaseDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.lastModifiedTime().toMillis() < expirationTime) {
                        try {
                            Files.delete(file);
                            counts[1]++;
                        } catch (IOException e) {
                            log.warn("Failed to delete expired file: {}", file, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // Do not delete base directory itself
                    if (dir.equals(tempBaseDir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    
                    // Check if directory is empty or expired
                    try {
                        if (Files.list(dir).findAny().isEmpty()) {
                            BasicFileAttributes attrs = Files.readAttributes(dir, BasicFileAttributes.class);
                            if (attrs.lastModifiedTime().toMillis() < expirationTime) {
                                Files.delete(dir);
                                counts[0]++;
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Failed to delete expired directory: {}", dir, e);
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
            
            if (counts[0] > 0 || counts[1] > 0) {
                log.info("Cleanup completed: deleted {} directories, {} files", counts[0], counts[1]);
            } else {
                log.debug("No expired files to cleanup");
            }
            
        } catch (IOException e) {
            log.error("Failed to cleanup temporary files", e);
        }
    }
    
    /**
     * Manual cleanup for specified directory (for immediate cleanup)
     */
    public void cleanupDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Immediately cleaned up directory: {}", dir);
        } catch (IOException e) {
            log.warn("Failed to cleanup directory: {}", dir, e);
        }
    }
}
