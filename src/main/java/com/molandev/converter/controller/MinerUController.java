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

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/convert/mineru")
@RequiredArgsConstructor
public class MinerUController {

    private final FileHandler fileHandler;
    private final CommandExecutor commandExecutor;
    private final ConcurrencyControlService concurrencyControl;
    
    // MinerU only supports PDF files
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".pdf");

    /**
     * Convert PDF using MinerU (multipart upload)
     * 
     * @param file PDF file
     * @param outputFormat Output format (md/json/zip, default md)
     */
    @PostMapping("/upload")
    public ResponseEntity<Resource> convertByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputFormat", defaultValue = "md") String outputFormat) {
        
        // Concurrency control
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
            
            Path outputDir = workDir.resolve("output");
            Files.createDirectories(outputDir);
            
            // Execute MinerU conversion command
            String[] command = {
                "mineru",
                "-p", inputFile.toString(),
                "-o", outputDir.toString(),
                "-b", "pipeline",
                "--source", "local"
            };
            
            commandExecutor.executeConversion(command, workDir);
            
            // Handle different output formats
            if ("zip".equalsIgnoreCase(outputFormat)) {
                // Pack all output files into a zip
                Path zipFile = zipDirectory(outputDir, workDir);
                String outputFilename = fileHandler.generateOutputFilename(originalFilename, ".zip");
                return buildFileResponse(zipFile, outputFilename);
            } else {
                // Find output file by extension
                String extension = "." + outputFormat;
                Path outputFile = fileHandler.findOutputFile(outputDir, extension);
                // Generate output filename for user
                String outputFilename = fileHandler.generateOutputFilename(originalFilename, extension);
                return buildFileResponse(outputFile, outputFilename);
            }
            
        } catch (IllegalArgumentException e) {
            // Parameter validation exception, pass to GlobalExceptionHandler
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request was interrupted");
        } catch (Exception e) {
            log.error("MinerU conversion failed", e);
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
     * Convert PDF using MinerU (URL download)
     * 
     * @param fileUrl PDF file URL
     * @param outputFormat Output format (md/json/zip, default md)
     */
    @PostMapping("/url")
    public ResponseEntity<Resource> convertByUrl(
            @RequestParam("fileUrl") String fileUrl,
            @RequestParam(value = "outputFormat", defaultValue = "md") String outputFormat) {
        
        // Concurrency control
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
            
            Path outputDir = workDir.resolve("output");
            Files.createDirectories(outputDir);
            
            // Execute MinerU conversion command
            String[] command = {
                "mineru",
                "-p", inputFile.toString(),
                "-o", outputDir.toString(),
                "-b", "pipeline",
                "--source", "local"
            };
            
            // Execute conversion
            try {
                commandExecutor.executeConversion(command, workDir);
            } catch (Exception e) {
                log.error("Document conversion failed", e);
                throw new RuntimeException("Document conversion failed: " + e.getMessage());
            }
            
            // Handle different output formats
            if ("zip".equalsIgnoreCase(outputFormat)) {
                // Pack all output files into a zip
                Path zipFile = zipDirectory(outputDir, workDir);
                String outputFilename = fileHandler.generateOutputFilename(originalFilename, ".zip");
                return buildFileResponse(zipFile, outputFilename);
            } else {
                // Find output file by extension
                String extension = "." + outputFormat;
                Path outputFile = fileHandler.findOutputFile(outputDir, extension);
                // Generate output filename for user
                String outputFilename = fileHandler.generateOutputFilename(originalFilename, extension);
                return buildFileResponse(outputFile, outputFilename);
            }
            
        } catch (IllegalArgumentException e) {
            // Parameter validation exception, pass to GlobalExceptionHandler
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request was interrupted");
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
                    ", MinerU only supports PDF files"
            );
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
    
    /**
     * Pack all files in output directory into a zip file
     * Flatten directory structure and rename UUID prefix to 'result'
     * 
     * @param outputDir Directory containing MinerU output files
     * @param workDir Working directory for temporary zip file
     * @return Path to the created zip file
     */
    private Path zipDirectory(Path outputDir, Path workDir) throws IOException {
        Path zipFile = workDir.resolve("output.zip");
        
        // UUID pattern: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        java.util.regex.Pattern uuidPattern = java.util.regex.Pattern.compile(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        );
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Files.walk(outputDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // Get only the filename, remove directory structure
                            String filename = file.getFileName().toString();
                            
                            // Replace UUID prefix with 'result'
                            String zipEntryName = uuidPattern.matcher(filename).replaceFirst("result");
                            
                            ZipEntry zipEntry = new ZipEntry(zipEntryName);
                            zos.putNextEntry(zipEntry);
                            
                            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = fis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, len);
                                }
                            }
                            
                            zos.closeEntry();
                        } catch (IOException e) {
                            log.error("Failed to add file to zip: {}", file, e);
                            throw new RuntimeException("Failed to create zip file", e);
                        }
                    });
        }
        
        log.info("Created zip file: {}", zipFile);
        return zipFile;
    }
}
