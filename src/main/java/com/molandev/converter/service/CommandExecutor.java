package com.molandev.converter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutor {
    
    private final TempFileCleanupService cleanupService;

    /**
     * 执行命令行转换
     */
    public Path executeConversion(String[] command, Path workDir) throws IOException, InterruptedException {
        log.info("Executing command: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(true);
        
        // Set character encoding to UTF-8
        pb.environment().put("LANG", "zh_CN.UTF-8");
        pb.environment().put("LC_ALL", "zh_CN.UTF-8");
        
        Process process = pb.start();
        
        // Read command output with UTF-8 encoding
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Command output: {}", line);
            }
        }
        
        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command execution timeout");
        }
        
        if (process.exitValue() != 0) {
            throw new RuntimeException("Command execution failed, exit code: " + process.exitValue());
        }
        
        return workDir;
    }

    /**
     * Create temporary working directory
     * Uses unified temporary file base directory for scheduled cleanup
     */
    public Path createTempWorkDir() throws IOException {
        Path baseDir = cleanupService.getTempBaseDir();
        String dirName = "work-" + UUID.randomUUID();
        Path workDir = baseDir.resolve(dirName);
        Files.createDirectories(workDir);
        log.debug("Created temporary working directory: {}", workDir);
        return workDir;
    }

    /**
     * Cleanup temporary directory (deprecated, use scheduled cleanup service)
     * Keep this method for compatibility but do not actually delete files
     * Temporary files will be cleaned up by TempFileCleanupService on schedule
     * 
     * @deprecated Use TempFileCleanupService for scheduled cleanup
     */
    @Deprecated
    public void cleanupDir(Path dir) {
        // Do not delete immediately, cleanup by scheduled task
        log.debug("Temporary files will be automatically cleaned up after {} minutes", cleanupService.getTempBaseDir());
    }
}
