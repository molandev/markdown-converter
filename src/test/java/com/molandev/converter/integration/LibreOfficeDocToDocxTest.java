package com.molandev.converter.integration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * LibreOffice Converter API Test
 * Used to verify document format conversions via LibreOffice
 */
public class LibreOfficeDocToDocxTest {

    private static final String BASE_URL = "http://localhost:10996";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  LibreOffice DOC to DOCX Test");
        System.out.println("========================================\n");

        // TODO: Change to your test file path
        String testFilePath = "D:\\md\\test.doc";
        String testFilePath1 = "D:\\md\\test.docx";

        if (args.length > 0) {
            testFilePath = args[0];
        }

        Path filePath = Paths.get(testFilePath);
        if (!Files.exists(filePath)) {
            System.err.println("❌ Test file not found: " + testFilePath);
            System.err.println("Please modify testFilePath variable or pass file path as argument");
            System.exit(1);
        }

        System.out.println("✅ Test file: " + filePath.toAbsolutePath());
        System.out.println("📡 Service URL: " + BASE_URL);
        System.out.println();

        try {
            // Test 1: Health Check
            testHealthCheck();

            // Test 2: File Upload Conversion (DOC -> DOCX)
            testFileUpload(filePath);

            // Test 3: DOCX to PDF Conversion
            testDocxToPdf( Paths.get(testFilePath1));

            // Test 4: URL Conversion (Optional)
            // testUrlConversion();

            System.out.println("\n========================================");
            System.out.println("  ✅ All Tests Passed!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("  ❌ Test Failed!");
            System.err.println("========================================");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Test 1: Health Check
     */
    private static void testHealthCheck() throws Exception {
        System.out.println("🔍 Test 1: Health Check...");

        String urlStr = BASE_URL + "/health";
        HttpURLConnection conn = createConnection(urlStr, "GET");
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            System.out.println("   ✅ Service is running (HTTP " + responseCode + ")");
        } else {
            throw new RuntimeException("Health check failed: HTTP " + responseCode);
        }
        System.out.println();
    }

    /**
     * Test 2: File Upload Conversion (DOC -> DOCX)
     */
    private static void testFileUpload(Path filePath) throws Exception {
        System.out.println("🔍 Test 2: File Upload Conversion (DOC -> DOCX)...");

        // Use format=docx to convert to DOCX
        String urlStr = BASE_URL + "/convert/libreoffice/upload?format=docx";
        String boundary = generateBoundary();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000); // LibreOffice may take longer

        // Write multipart data
        try (OutputStream os = conn.getOutputStream()) {
            writeMultipartData(os, filePath, boundary);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // Read response content
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // Generate output filename (replace extension with .docx)
            String originalFilename = filePath.getFileName().toString();
            String outputFilename = originalFilename;
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                outputFilename = originalFilename.substring(0, dotIndex) + ".docx";
            } else {
                outputFilename = originalFilename + ".docx";
            }
            System.out.println("   Output File: " + outputFilename);

            // Save converted file to D:/md directory
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ File conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("File upload conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    /**
     * Test 3: DOCX to PDF Conversion
     */
    private static void testDocxToPdf(Path filePath) throws Exception {
        System.out.println("🔍 Test 3: DOCX to PDF Conversion...");

        // Use format=pdf to convert to PDF
        String urlStr = BASE_URL + "/convert/libreoffice/upload?format=pdf";
        String boundary = generateBoundary();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000); // LibreOffice may take longer

        // Write multipart data
        try (OutputStream os = conn.getOutputStream()) {
            writeMultipartData(os, filePath, boundary);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // Read response content
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // Generate output filename (replace extension with .pdf)
            String originalFilename = filePath.getFileName().toString();
            String outputFilename = originalFilename;
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                outputFilename = originalFilename.substring(0, dotIndex) + ".pdf";
            } else {
                outputFilename = originalFilename + ".pdf";
            }
            System.out.println("   Output File: " + outputFilename);

            // Save converted file to D:/md directory
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ DOCX to PDF conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("DOCX to PDF conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    /**
     * Test 4: URL Conversion (Example)
     */
    @SuppressWarnings("unused")
    private static void testUrlConversion() throws Exception {
        System.out.println("🔍 Test 3: URL Conversion (Optional)...");

        // TODO: Replace with your document URL
        String fileUrl = "http://192.168.1.100:5143/test.doc";

        String urlStr = BASE_URL + "/convert/libreoffice/url?fileUrl=" +
                java.net.URLEncoder.encode(fileUrl, "UTF-8") + "&format=docx";

        HttpURLConnection conn = createConnection(urlStr, "POST");
        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // Read response content
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // Extract filename from URL
            String outputFilename = extractFilenameFromUrl(fileUrl, ".docx");
            System.out.println("   Output File: " + outputFilename);

            // Save converted file to D:/md directory
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ URL conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("URL conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    // ==================== Helper Methods ====================

    private static HttpURLConnection createConnection(String urlStr, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(120000);
        return conn;
    }

    private static String generateBoundary() {
        return "----WebKitFormBoundary" + System.currentTimeMillis();
    }

    private static void writeMultipartData(OutputStream os, Path filePath, String boundary) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

        // Add file field
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filePath.getFileName().toString()).append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        // Write file content
        Files.copy(filePath, os);
        os.flush();

        writer.append("\r\n").flush();
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.close();
    }

    private static String readErrorResponse(InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return "No error message";
        }
        return new String(errorStream.readAllBytes(), "UTF-8");
    }

    /**
     * Extract filename from URL and replace extension
     */
    private static String extractFilenameFromUrl(String fileUrl, String newExtension) {
        // Extract filename from URL path
        String path = fileUrl;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            path = path.substring(lastSlash + 1);
        }
        // Remove query parameters
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        // Replace extension
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            path = path.substring(0, dotIndex) + newExtension;
        } else {
            path = path + newExtension;
        }
        return path.isEmpty() ? "converted" + newExtension : path;
    }
}
