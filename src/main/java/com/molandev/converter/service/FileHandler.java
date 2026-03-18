package com.molandev.converter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileHandler {

    /**
     * Save multipart file to specified directory, using UUID filename
     * 
     * @return Array: [0]=file path after save, [1]=original filename
     */
    public String[] saveMultipartFile(MultipartFile file, Path workDir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "upload_file";
        }
        
        // 提取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 使用UUID生成文件名
        String uuidFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = workDir.resolve(uuidFilename);
        
        file.transferTo(targetPath.toFile());
        log.info("Saved uploaded file: {} -> {}", originalFilename, targetPath);
        
        return new String[]{targetPath.toString(), originalFilename};
    }

    /**
     * Download file from URL to specified directory, using UUID filename
     * 
     * @return Array: [0]=file path after save, [1]=original filename
     */
    public String[] downloadFile(String fileUrl, Path workDir) throws IOException {
        URL url = new URL(fileUrl);
        String originalFilename = extractFilename(fileUrl);
        
        // 提取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 使用UUID生成文件名
        String uuidFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = workDir.resolve(uuidFilename);
        
        try (InputStream in = url.openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Downloaded file: {} -> {}", fileUrl, targetPath);
        
        return new String[]{targetPath.toString(), originalFilename};
    }

    /**
     * Extract filename from URL
     */
    private String extractFilename(String fileUrl) {
        String path = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        return path.isEmpty() ? "downloaded_file" : path;
    }

    /**
     * Find converted output file (recursive search in subdirectories)
     */
    public Path findOutputFile(Path outputDir, String extension) throws IOException {
        try (var stream = Files.walk(outputDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(extension))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("Output file not found: *" + extension));
        }
    }

    /**
     * Find first file in directory
     */
    public Path findFirstFile(Path dir) throws IOException {
        try (var stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("Output file not found"));
        }
    }
    
    /**
     * Generate output filename based on original filename and new extension
     */
    public String generateOutputFilename(String originalFilename, String newExtension) {
        int dotIndex = originalFilename.lastIndexOf('.');
        String baseName = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
        return baseName + newExtension;
    }
}
