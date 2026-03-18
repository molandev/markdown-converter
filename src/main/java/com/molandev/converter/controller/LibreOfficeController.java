package com.molandev.converter.controller;

import com.molandev.converter.service.CommandExecutor;
import com.molandev.converter.service.ConcurrencyControlService;
import com.molandev.converter.service.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/convert/libreoffice")
@RequiredArgsConstructor
public class LibreOfficeController {

    private final FileHandler fileHandler;
    private final CommandExecutor commandExecutor;
    private final ConcurrencyControlService concurrencyControl;

    /**
     * 使用LibreOffice转换文档（multipart上传）
     * 
     * @param file 上传的文件
     * @param format 目标格式（pdf/docx/xlsx等，默认pdf）
     */
    @PostMapping("/upload")
    public ResponseEntity<Resource> convertByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", defaultValue = "pdf") String format) {
        
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
            
            // 执行LibreOffice转换命令
            String[] command = {
                "libreoffice",
                "--headless",
                "--convert-to", format,
                "--outdir", workDir.toString(),
                inputFile.toString()
            };
            
            commandExecutor.executeConversion(command, workDir);
            
            // 查找转换后的文件
            Path outputFile = fileHandler.findOutputFile(workDir, "." + format);
            
            // 生成返回给用户的文件名
            String outputFilename = fileHandler.generateOutputFilename(originalFilename, "." + format);
            
            return buildFileResponse(outputFile, outputFilename);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("请求被中断");
        } catch (Exception e) {
            log.error("LibreOffice conversion failed", e);
            throw new RuntimeException("转换失败: " + e.getMessage());
        } finally {
            if (acquired) {
                concurrencyControl.release();
            }
            // 不再立即清理，由定时任务负责
            // commandExecutor.cleanupDir(workDir);
        }
    }

    /**
     * 使用LibreOffice转换文档（URL下载）
     * 
     * @param fileUrl 文件URL
     * @param format 目标格式（pdf/docx/xlsx等，默认pdf）
     */
    @PostMapping("/url")
    public ResponseEntity<Resource> convertByUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "format", defaultValue = "pdf") String format) {
        
        // 并发控制
        boolean acquired = false;
        Path workDir = null;
        try {
            acquired = concurrencyControl.acquire();
            if (!acquired) {
                throw new RuntimeException("System is busy, please try again later");
            }
            
            workDir = commandExecutor.createTempWorkDir();
            
            // 下载文件
            String[] fileInfo;
            try {
                fileInfo = fileHandler.downloadFile(fileUrl, workDir);
            } catch (Exception e) {
                log.error("File download failed: {}", fileUrl, e);
                throw new RuntimeException("File download failed: " + e.getMessage() + ", please check if the URL is correct");
            }
            
            Path inputFile = Path.of(fileInfo[0]);
            String originalFilename = fileInfo[1];
            
            // 执行LibreOffice转换命令
            String[] command = {
                "libreoffice",
                "--headless",
                "--convert-to", format,
                "--outdir", workDir.toString(),
                inputFile.toString()
            };
            
            // Execute conversion
            try {
                commandExecutor.executeConversion(command, workDir);
            } catch (Exception e) {
                log.error("Document conversion failed", e);
                throw new RuntimeException("Document conversion failed: " + e.getMessage());
            }
            
            // 查找转换后的文件
            Path outputFile = fileHandler.findOutputFile(workDir, "." + format);
            
            // 生成返回给用户的文件名
            String outputFilename = fileHandler.generateOutputFilename(originalFilename, "." + format);
            
            return buildFileResponse(outputFile, outputFilename);
            
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
            // 不再立即清理，由定时任务负责
            // commandExecutor.cleanupDir(workDir);
        }
    }

    private ResponseEntity<Resource> buildFileResponse(Path file, String filename) throws IOException {
        // First read file content to byte array to avoid access issues after temp file deletion
        byte[] fileContent = Files.readAllBytes(file);
        Resource resource = new ByteArrayResource(fileContent);
        
        // URL-encode Chinese filename to avoid HTTP header encoding errors
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20"); // Encode space as %20 instead of +
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileContent.length)
                .body(resource);
    }
}
