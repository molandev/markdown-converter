package com.molandev.converter.controller;

import com.molandev.converter.service.CommandExecutor;
import com.molandev.converter.service.ConcurrencyControlService;
import com.molandev.converter.service.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/convert/markitdown")
@RequiredArgsConstructor
public class MarkItDownController {

    private final FileHandler fileHandler;
    private final CommandExecutor commandExecutor;
    private final ConcurrencyControlService concurrencyControl;
    
    // MarkItDown supported file types
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".pdf", ".docx", ".pptx", ".xlsx", ".xls"
    );

    /**
     * Convert document to Markdown using MarkItDown (multipart upload)
     * 
     * @param file Document file
     * @param keepDataUris Keep images in document (default false)
     */
    @PostMapping("/upload")
    public ResponseEntity<Resource> convertByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "keepDataUris", defaultValue = "false") boolean keepDataUris) {
        
        // 并发控制
        boolean acquired = false;
        Path workDir = null;
        try {
            acquired = concurrencyControl.acquire();
            if (!acquired) {
                throw new RuntimeException("System is busy, please try again later");
            }
            
            workDir = commandExecutor.createTempWorkDir();
            String[] fileInfo = fileHandler.saveMultipartFile(file, workDir);
            Path inputFile = Path.of(fileInfo[0]);
            String originalFilename = fileInfo[1];
            
            // Validate file type
            validateFileType(originalFilename);
            
            String baseName = inputFile.getFileName().toString();
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }
            Path outputFile = workDir.resolve(baseName + ".md");
            
            // Build command
            List<String> commandList = new ArrayList<>();
            commandList.add("markitdown");
            if (keepDataUris) {
                commandList.add("--keep-data-uris");
            }
            commandList.add(inputFile.toString());
            commandList.add("-o");
            commandList.add(outputFile.toString());
            
            String[] command = commandList.toArray(new String[0]);
            
            commandExecutor.executeConversion(command, workDir);
            
            // Verify output file exists
            if (!Files.exists(outputFile)) {
                throw new RuntimeException("Output file was not generated");
            }
            
            // 生成返回给用户的文件名
            String outputFilename = fileHandler.generateOutputFilename(originalFilename, ".md");
            
            return buildFileResponse(outputFile, outputFilename);
            
        } catch (IllegalArgumentException e) {
            // Parameter validation exception, pass to GlobalExceptionHandler
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request was interrupted");
        } catch (Exception e) {
            log.error("MarkItDown conversion failed", e);
            throw new RuntimeException("Conversion failed: " + e.getMessage());
        } finally {
            if (acquired) {
                concurrencyControl.release();
            }
            // Do not cleanup immediately, scheduled task will handle it
            // commandExecutor.cleanupDir(workDir);
        }
    }

    /**
     * Convert document to Markdown using MarkItDown (URL download)
     * 
     * @param fileUrl Document file URL
     * @param keepDataUris Keep images in document (default false)
     */
    @PostMapping("/url")
    public ResponseEntity<Resource> convertByUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "keepDataUris", defaultValue = "false") boolean keepDataUris) {
        
        // 并发控制
        boolean acquired = false;
        Path workDir = null;
        try {
            acquired = concurrencyControl.acquire();
            if (!acquired) {
                throw new RuntimeException("System is busy, please try again later");
            }
            
            workDir = commandExecutor.createTempWorkDir();
            
            // Download file
            String[] fileInfo;
            try {
                fileInfo = fileHandler.downloadFile(fileUrl, workDir);
            } catch (Exception e) {
                log.error("File download failed: {}", fileUrl, e);
                throw new RuntimeException("File download failed: " + e.getMessage() + ", please check if the URL is correct");
            }
            
            Path inputFile = Path.of(fileInfo[0]);
            String originalFilename = fileInfo[1];
            
            // Validate file type
            validateFileType(originalFilename);
            
            String baseName = inputFile.getFileName().toString();
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }
            Path outputFile = workDir.resolve(baseName + ".md");
            
            // Build command
            List<String> commandList = new ArrayList<>();
            commandList.add("markitdown");
            if (keepDataUris) {
                commandList.add("--keep-data-uris");
            }
            commandList.add(inputFile.toString());
            commandList.add("-o");
            commandList.add(outputFile.toString());
            
            String[] command = commandList.toArray(new String[0]);
            
            // Execute conversion
            try {
                commandExecutor.executeConversion(command, workDir);
            } catch (Exception e) {
                log.error("Document conversion failed", e);
                throw new RuntimeException("Document conversion failed: " + e.getMessage());
            }
            
            // 验证输出文件存在
            if (!Files.exists(outputFile)) {
                throw new RuntimeException("Document conversion failed: Output file was not generated");
            }
            
            // 生成返回给用户的文件名
            String outputFilename = fileHandler.generateOutputFilename(originalFilename, ".md");
            
            return buildFileResponse(outputFile, outputFilename);
            
        } catch (IllegalArgumentException e) {
            // 参数校验异常直接抛出，交给GlobalExceptionHandler处理
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断");
        } catch (RuntimeException e) {
            // Business exception (already contains friendly error message)
            throw e;
        } catch (Exception e) {
            log.error("Request processing failed", e);
            throw new RuntimeException("Request processing failed: " + e.getMessage());
        } finally {
            if (acquired) {
                concurrencyControl.release();
            }
            // Do not cleanup immediately, scheduled task will handle it
            // commandExecutor.cleanupDir(workDir);
        }
    }
    
    /**
     * Validate file type
     */
    private void validateFileType(String filename) {
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex).toLowerCase();
        }
        
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + extension + 
                    ", only supports: " + String.join(", ", SUPPORTED_EXTENSIONS)
            );
        }
    }

    private ResponseEntity<Resource> buildFileResponse(Path file, String filename) {
        // Use FileSystemResource to avoid large files consuming memory
        // File will be deleted by cleanup service after configured time
        Resource resource = new FileSystemResource(file);
        
        // URL-encode Chinese filename to avoid HTTP header encoding errors
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20"); // Encode space as %20 instead of +
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
